package app.wallet.service;

import app.exception.DomainException;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.property.WalletProperty;
import app.wallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {
    private static final String SMART_WALLET_LTD="Smart Wallet LTD";
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final WalletProperty walletProperty;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService, WalletProperty walletProperty) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
        this.walletProperty = walletProperty;
    }

    public void createNewWallet(User user) {
        Wallet wallet =  walletRepository.save(initializeWallet(user));

        log.info("Successfully created new wallet with id [%s] and balance [%.2f]"
                .formatted(wallet.getId(), wallet.getBalance()));
    }

    @Transactional
    public Transaction topUp(UUID ID, BigDecimal amount){
        Wallet wallet = getWalletById(ID);

        String transactionDescription="Top up %.2f".formatted(amount.doubleValue());
        if(wallet.getStatus() == WalletStatus.INACTIVE){
            return
                    transactionService.createNewTransaction(wallet.getOwner(),
                            SMART_WALLET_LTD,
                            ID.toString(),
                            amount,
                            wallet.getBalance(),
                            wallet.getCurrency(),
                            TransactionType.DEPOSIT,
                            TransactionStatus.FAILED,
                            transactionDescription,
                            "Invalid wallet"
                            );
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        return transactionService.createNewTransaction(wallet.getOwner(),
                SMART_WALLET_LTD,
                ID.toString(),
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null);
    }
    
    private Wallet getWalletById(UUID ID){
        return walletRepository.findById(ID).orElseThrow(
                ()->new DomainException("Wallet with id [%s] does not exist.".formatted(ID)));
    }


    private Wallet initializeWallet(User user) {
        return Wallet.builder()
                .owner(user)
                .status(walletProperty.getDefaultStatus())
                .balance(walletProperty.getDefaultBalance())
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                        .build();
    }
}
