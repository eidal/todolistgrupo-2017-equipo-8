import org.junit.*;
import static org.junit.Assert.*;

import play.db.Database;
import play.db.Databases;
import play.db.jpa.*;

import java.lang.IllegalArgumentException;

import play.Logger;

import java.sql.*;

import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.operation.*;
import java.io.FileInputStream;

import play.inject.guice.GuiceApplicationBuilder;
import play.inject.Injector;
import play.inject.guice.GuiceInjectorBuilder;
import play.Environment;

import models.Etiqueta;
import models.EtiquetaRepository;
import models.JPAEtiquetaRepository;

public class EtiquetaTest {

  static Database db;
  static private Injector injector;

  //Se ejecuta sólo una vez, al principio de todos los tests
  @BeforeClass
  static public void initApplication(){
    GuiceApplicationBuilder guiceApplicationBuilder =
      new GuiceApplicationBuilder().in(Environment.simple());
    injector=guiceApplicationBuilder.injector();
    db=injector.instanceOf(Database.class);
    //Necesario para inicializar JPA
    injector.instanceOf(JPAApi.class);
  }

  @Before
  public void initData() throws Exception {
    JndiDatabaseTester databaseTester = new JndiDatabaseTester("DBTest");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("test/resources/usuarios_dataset.xml"));
    databaseTester.setDataSet(initialDataSet);
    databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
    databaseTester.onSetup();
  }

  private EtiquetaRepository newEtiquetaRepository(){
    return injector.instanceOf(EtiquetaRepository.class);
  }


  @Test
  public void testCrearEtiquetaColor(){
    String color="#d93f0b";
    Etiqueta etiqueta =new Etiqueta(color);
    assertEquals(etiqueta.getColor(),color);
    assertEquals(etiqueta.getNombre(),"");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testCrearEtiquetaColorIncorrectoExcepcion(){
    String color="d93f0b";
    Etiqueta etiqueta =new Etiqueta(color);
  }

  @Test
  public void testCrearEtiquetaColorNombre(){
    String color="#d93f0b";
    String nombre="urgente";
    Etiqueta etiqueta =new Etiqueta(color,nombre);
    assertEquals(etiqueta.getColor(),color);
    assertEquals(etiqueta.getNombre(),nombre);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testCrearEtiquetaColorIncorrectoNombreExcepcion(){
    String color="d93f0b";
    String nombre="urgente";
    Etiqueta etiqueta =new Etiqueta(color,nombre);
    assertEquals(etiqueta.getColor(),color);
    assertEquals(etiqueta.getNombre(),nombre);
  }

  @Test
  public void testModificarEtiqueta(){
    String color="#d93f0b";
    String color2="#ffffff";
    String nombre="urgente";
    Etiqueta etiqueta =new Etiqueta(color);
    assertEquals(etiqueta.getColor(),color);
    assertEquals(etiqueta.getNombre(),"");
    etiqueta.setNombre(nombre);
    etiqueta.setColor(color2);
    assertEquals(etiqueta.getColor(),color2);
    assertEquals(etiqueta.getNombre(),nombre);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testModificarEtiquetaColorIncorrecto(){
    String color="#d93f0b";
    String color2="ffffff";
    Etiqueta etiqueta =new Etiqueta(color);
    etiqueta.setColor(color2);
  }

  @Test
  public void testCompruebaColor(){
    Boolean correcto=Etiqueta.compruebaColor("#d93f0b");
    Boolean incorrecto=Etiqueta.compruebaColor("jd93f0b");
    assertTrue(correcto);
    assertTrue(!incorrecto);
  }

}
