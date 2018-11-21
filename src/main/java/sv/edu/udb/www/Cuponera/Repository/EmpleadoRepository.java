package sv.edu.udb.www.Cuponera.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sv.edu.udb.www.Cuponera.Entities.Empleado;
import sv.edu.udb.www.Cuponera.Entities.Empresa;

@Repository("EmpleadoRepository")
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

	public abstract List<Empleado> findByEmpresa(Empresa empresa);
}
