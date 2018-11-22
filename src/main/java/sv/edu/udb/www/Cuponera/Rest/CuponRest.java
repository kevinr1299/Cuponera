package sv.edu.udb.www.Cuponera.Rest;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sv.edu.udb.www.Cuponera.Entities.Cliente;
import sv.edu.udb.www.Cuponera.Entities.Cupon;
import sv.edu.udb.www.Cuponera.Entities.Usuario;
import sv.edu.udb.www.Cuponera.Repository.ClienteRepository;
import sv.edu.udb.www.Cuponera.Repository.CuponRepository;
import sv.edu.udb.www.Cuponera.Repository.EstadoCuponRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;
import sv.edu.udb.www.Cuponera.Utils.CanjearCupon;

@RestController
@RequestMapping("/api/cupon")
public class CuponRest {

	@Autowired
	@Qualifier("CuponRepository")
	CuponRepository cuponRepository;
	
	@Autowired
	@Qualifier("EstadoCuponRepository")
	EstadoCuponRepository estadoRepository;
	
	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository usuarioRepository;
	
	@Autowired
	@Qualifier("ClienteRepository")
	ClienteRepository clienteRepository;
	
	@PostMapping("/canjear")
	public ResponseEntity<?> canjearCupon(@RequestBody CanjearCupon canjear){
		try {
			if(cuponRepository.existsById(canjear.getCupon())) {
				Cupon cupon = cuponRepository.getOne(canjear.getCupon());
				if(cupon.getEstadoCupon().getId() == 1) {
					if(cupon.getCliente().getDui().equals(canjear.getDui())) {
						cupon.setEstadoCupon(estadoRepository.getOne(2));
						cupon.setFechaCanje(new Date());
						cuponRepository.save(cupon);
						return ResponseEntity.ok().body("Cupon canjeado");
					}else {
						return ResponseEntity.status(400).body("El DUI del propietario del cupon y el ingresado no coinciden");
					}
				}else if(cupon.getEstadoCupon().getId() == 2){
					return ResponseEntity.status(400).body("El cupon ya esta canjeado");
				}else {
					return ResponseEntity.status(400).body("El cupon ya esta canjeado");
				}
			}else {
				return ResponseEntity.status(404).body("No se encontro ningun cupon");
			}
		}catch(Exception ex) {
			System.out.println(ex);
			return ResponseEntity.status(500).body("No se pudo canjear el cupon");
		}
	}
	
	@GetMapping("/pin/{codigo}")
	public String obtenerCupon(@PathVariable("codigo") int codigo) {
		Cliente cliente = clienteRepository.getOne(codigo);
		if(cliente.getTarjeta().isEmpty()) {
			return "";
		}else {
			return "Si";
		}
	}

}
