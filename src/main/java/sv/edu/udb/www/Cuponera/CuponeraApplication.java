package sv.edu.udb.www.Cuponera;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import sv.edu.udb.www.Cuponera.Controller.PromocionController;

@SpringBootApplication
@ComponentScan({"sv.edu.udb.www.Cuponera","sv.edu.udb.www.Cuponera.Controller"})
public class CuponeraApplication {

	public static void main(String[] args) {
		new File(PromocionController.uploadDirectory).mkdir();
		SpringApplication.run(CuponeraApplication.class, args);
	}
}
