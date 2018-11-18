package sv.edu.udb.www.Cuponera.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import sv.edu.udb.www.Cuponera.Entities.Usuario;
import sv.edu.udb.www.Cuponera.Repository.TipoRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;

@Service("UsuarioService")
public class UsuarioService implements UserDetailsService {

	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository repository;
	
	@Autowired
	@Qualifier("TipoRepository")
	TipoRepository tipo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			Usuario usuario = repository.findByCorreo(username);
			System.out.println(usuario.getId());
			List<GrantedAuthority> lista = new ArrayList<>();
			lista.add(new SimpleGrantedAuthority(usuario.getTipoUsuario().getDescripcion()));
			return new User(username, usuario.getPassword(), usuario.isVerificado(), true, true, true, lista);
		}catch(Exception ex) {
			System.out.println(ex);
			return null;
		}
	}
	
}
