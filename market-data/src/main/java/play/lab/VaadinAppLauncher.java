package play.lab;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(variant = Lumo.DARK)
public class VaadinAppLauncher implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(VaadinAppLauncher.class, args);
    }
}
