import org.junit.*;
import static org.junit.Assert.*;

import play.db.jpa.*;

import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.operation.*;
import java.io.FileInputStream;

import play.inject.guice.GuiceApplicationBuilder;
import play.inject.Injector;
import play.inject.guice.GuiceInjectorBuilder;
import play.Environment;

import play.db.jpa.JPAApi;

import java.util.Date;
import java.text.SimpleDateFormat;

import models.Tablero;

import services.TableroService;
import services.TableroServiceException;
import services.UsuarioService;

public class ServicioAsignarParticipanteTableroListarTablerosTest{
  static private Injector injector;

  //Se ejecuta  sólo una vez, al princiio de todos los tests
  @BeforeClass
  static public void initApplication(){
    GuiceApplicationBuilder guiceApplicationBuilder =
      new GuiceApplicationBuilder().in(Environment.simple());
    injector=guiceApplicationBuilder.injector();
    //Instanciamos un JPAApi para que inicialice JPA
    injector.instanceOf(JPAApi.class);
  }

  @Before
  public void initData() throws Exception{
    JndiDatabaseTester databaseTester=new JndiDatabaseTester("DBTest");
    IDataSet initialDataSet=new FlatXmlDataSetBuilder().build(new FileInputStream("test/resources/usuarios_dataset.xml"));
    databaseTester.setDataSet(initialDataSet);
    databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
    databaseTester.onSetup();
  }

  private TableroService newTableroService(){
    return injector.instanceOf(TableroService.class);
  }

  //comprueba asignación participante a tablero
  @Test
  public void asignarParticipanteTableroTest(){
    long idUsuario=1001L;
    TableroService tableroService=newTableroService();
    Tablero tablero=tableroService.creaTablero(idUsuario,"tablero 1");
    tablero=tableroService.addParticipanteaTablero(tablero.getId(),idUsuario);
    assertEquals(1,tablero.getParticipantes().size());
  }

  //comprueba excepción asignar participante no existente a tableros
  @Test(expected=TableroServiceException.class)
  public void asignarParticipanteNoExisteTableroExcepcionTest(){
    long idUsuario=1000L;
    long idUsuario2=5000L;
    TableroService tableroService=newTableroService();
    Tablero tablero=tableroService.creaTablero(idUsuario,"tablero 1");
    tablero=tableroService.addParticipanteaTablero(tablero.getId(),idUsuario2);
  }
}
