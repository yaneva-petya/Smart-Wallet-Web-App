package app.user.property;


import app.user.model.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "users")
public class UserProperty {
    private UserRole defaultRole;
    private boolean defaultAccountState;

}
