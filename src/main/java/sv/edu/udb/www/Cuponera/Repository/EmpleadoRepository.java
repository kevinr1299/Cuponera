package sv.edu.udb.www.Cuponera.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sv.edu.udb.www.Cuponera.Entities.Empleado;

@Repository("EmpleadoRepository")
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

}
