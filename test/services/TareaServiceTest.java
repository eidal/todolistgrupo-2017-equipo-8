import org.junit.*;
import static org.junit.Assert.*;

import play.db.jpa.*;

import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.operation.*;
import java.io.FileInputStream;

import java.util.List;
import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Calendar;

import models.Usuario;
import models.Tarea;
import models.Tablero;
import models.Etiqueta;

import play.inject.guice.GuiceApplicationBuilder;
import play.inject.Injector;
import play.inject.guice.GuiceInjectorBuilder;
import play.Environment;

import play.Logger;

import services.UsuarioService;
import services.UsuarioServiceException;
import services.TareaService;
import services.TareaServiceException;
import services.TableroService;
import services.TableroServiceException;
import services.EtiquetaService;
import services.EtiquetaServiceException;

public class TareaServiceTest {
  static private Injector injector;

  @BeforeClass
  static public void initApplication() {
    GuiceApplicationBuilder guiceApplicationBuilder =
      new GuiceApplicationBuilder().in(Environment.simple());
    injector=guiceApplicationBuilder.injector();
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

  private TareaService newTareaService() {
    return injector.instanceOf(TareaService.class);
  }
  private UsuarioService newUsuarioService() {
    return injector.instanceOf(UsuarioService.class);
  }
  private TableroService newTableroService() {
    return injector.instanceOf(TableroService.class);
  }

  private EtiquetaService newEtiquetaService() {
    return injector.instanceOf(EtiquetaService.class);
  }

  // Test #19: allTareasUsuarioEstanOrdenadas
  @Test
  public void allTareasUsuarioEstanOrdenadas() {
    TareaService tareaService = newTareaService();
    List<Tarea> tareas = tareaService.allTareasUsuario(1000L);
    assertEquals("Renovar DNI", tareas.get(0).getTitulo());
    assertEquals("Práctica 1 MADS", tareas.get(1).getTitulo());
  }

  // Test #19: allTareasUsuarioOrdenadasFechaLimite
  @Test
  public void allTareasUsuarioEstanOrdenadasFechaLimite() {
    TareaService tareaService = newTareaService();
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
    long idUsuario=1000L;
    Calendar cal=Calendar.getInstance();
    cal.add(Calendar.DATE,1);
    Date fechaManana= cal.getTime();
    Date fechaHoy=new Date();
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler2",sdf.format(fechaManana).toString(), null);
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler3",sdf.format(fechaHoy).toString(), null);
    List<Tarea> tareas = tareaService.allTareasUsuarioOrdenadasFechaLimite(idUsuario);
    assertEquals("Renovar DNI", tareas.get(0).getTitulo());
    assertEquals("Práctica 1 MADS", tareas.get(1).getTitulo());
    assertEquals("Pagar el alquiler3", tareas.get(2).getTitulo());
    assertEquals("Pagar el alquiler2", tareas.get(3).getTitulo());
  }

  // Test #20: exceptionSiUsuarioNoExisteRecuperandoSusTareas
  @Test(expected = TareaServiceException.class)
  public void crearNuevoUsuarioLoginRepetidoLanzaExcepcion(){
    TareaService tareaService = newTareaService();
    List<Tarea> tareas = tareaService.allTareasUsuario(2000L);
  }

  // Test #21: nuevaTareaUsuario
  @Test
  public void nuevaTareaUsuario(){
    TareaService tareaService=newTareaService();
    long idUsuario=1000L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler", null, null);
    assertEquals(3,tareaService.allTareasUsuario(1000L).size());
  }

  // Test : nuevaTareaUsuarioConFechaLimite
  @Test
  public void nuevaTareaUsuarioConFechaLimite(){
    TareaService tareaService=newTareaService();
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
    Date fecha=new Date();
    String fechaLimite=sdf.format(fecha).toString();
    Logger.info("fecha limite: "+fechaLimite);
    long idUsuario=1000L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",fechaLimite, null);
    assertEquals(3,tareaService.allTareasUsuario(1000L).size());
  }

  // Test : nuevaTareaUsuarioConFechaLimiteNull
  @Test
  public void nuevaTareaUsuarioConFechaLimiteNull(){
    TareaService tareaService=newTareaService();
    long idUsuario=1000L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",null, null);
    assertEquals(3,tareaService.allTareasUsuario(1000L).size());
  }

  // Test : nuevaTareaUsuarioConFechaLimiteIncorrectaExcepcion
  @Test(expected=TareaServiceException.class)
  public void nuevaTareaUsuarioConFechaLimiteIncorrectaExcepcion(){
    TareaService tareaService=newTareaService();
    String fechaLimite="25-13-2018";
    long idUsuario=1000L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",fechaLimite, null);
  }

  // Test : nuevaTareaUsuarioConFechaLimiteAnteriorExcepcion
  @Test(expected=TareaServiceException.class)
  public void nuevaTareaUsuarioConFechaLimiteAnteriorExcepcion(){
    TareaService tareaService=newTareaService();
    String fechaLimite="25-10-2010";
    long idUsuario=1000L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",fechaLimite, null);
  }

  // Test #22: modificación de tareas
  @Test
  public void modificacionTarea(){
    TareaService tareaService=newTareaService();
    long idTarea=1000L;
    tareaService.modificaTarea(idTarea,"Pagar el alquiler", null, null);
    Tarea tarea=tareaService.obtenerTarea(idTarea);
    assertEquals("Pagar el alquiler",tarea.getTitulo());
  }

  // Test: modificación fechaLimite en Tarea
  @Test
  public void modificacionFechaLimiteTarea(){
    TareaService tareaService=newTareaService();
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
    try{
      Date fecha=new Date();
      String fechaLimite=sdf.format(fecha).toString();
      long idTarea=1000L;
      tareaService.modificaTarea(idTarea,"Pagar el alquiler",fechaLimite, null);
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      assertEquals("Pagar el alquiler",tarea.getTitulo());
      assertEquals(fechaLimite,sdf.format(tarea.getFechaLimite()).toString());
    } catch (Exception e) {}
  }

  // Test: modificación fechaLimite null en Tarea
  @Test
  public void modificacionFechaLimiteTareaNull(){
    TareaService tareaService=newTareaService();
    Date fecha;
    long idTarea=1000L;
    Tarea tarea=tareaService.obtenerTarea(idTarea);
    fecha=tarea.getFechaLimite();
    tareaService.modificaTarea(idTarea,"Pagar el alquiler",null, null);
    tarea=tareaService.obtenerTarea(idTarea);
    assertEquals("Pagar el alquiler",tarea.getTitulo());
    assertEquals(fecha,tarea.getFechaLimite());
  }

  // Test: modificación fechaLimite incorrecta en Tarea excepcion
  @Test(expected=TareaServiceException.class)
  public void modificacionFechaLimiteIncorrectaTareaExcepcion(){
    TareaService tareaService=newTareaService();
    String fechaLimite="28-13-2018";
    long idTarea=1000L;
    tareaService.modificaTarea(idTarea,"Pagar el alquiler",fechaLimite, null);
  }

  // Test: modificación fechaLimite anterior en Tarea excepcion
  @Test(expected=TareaServiceException.class)
  public void modificacionFechaLimiteAnteriorTareaExcepcion(){
    TareaService tareaService=newTareaService();
    String fechaLimite="28-10-2010";
    long idTarea=1000L;
    tareaService.modificaTarea(idTarea,"Pagar el alquiler",fechaLimite, null);
  }

  // Test #23: borrado tarea
  @Test
  public void borradoTarea(){
    TareaService tareaService=newTareaService();
    long idTarea=1000L;
    tareaService.borraTarea(idTarea);
    assertNull(tareaService.obtenerTarea(idTarea));
  }

  @Test
  public void ponerTareaTerminada(){
    TareaService tareaService=newTareaService();
    long idTarea =1000L;
    tareaService.tareaTerminada(idTarea);
    Tarea tarea=tareaService.obtenerTarea(idTarea);
    assertTrue(tarea.getTerminada());
  }

  @Test(expected = TareaServiceException.class)
  public void ponerTareaNoexisteTerminada(){
    TareaService tareaService=newTareaService();
    long idTarea =8000L;
    tareaService.tareaTerminada(idTarea);
  }

  @Test
  public void listarTareasNoTerminadasUsuario(){
    TareaService tareaService=newTareaService();
    long idUsuario=1000L;
    long idTarea =1001L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler", null, null);
    tareaService.nuevaTarea(idUsuario,"Pagar la coca", null, null);
    tareaService.tareaTerminada(idTarea);
    assertEquals(1,tareaService.tareasTerminadas(1000L).size());
    assertEquals(4,tareaService.allTareasUsuario(1000L).size());
  }

  // Test : nuevaTareaUsuarioConDescripcion
  @Test
  public void nuevaTareaUsuarioConDescripcion(){
    TareaService tareaService=newTareaService();
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
    Date fecha=new Date();
    String fechaLimite=sdf.format(fecha).toString();
    Logger.info("fecha limite: "+fechaLimite);
    long idUsuario=1000L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",fechaLimite, "descripcion");
    assertEquals(3,tareaService.allTareasUsuario(1000L).size());
  }
// Test : nuevaTareaUsuarioConDescripcionNull
  @Test
  public void nuevaTareaUsuarioConDescripcionNull(){
    TareaService tareaService=newTareaService();
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
    Date fecha=new Date();
    String fechaLimite=sdf.format(fecha).toString();
    Logger.info("fecha limite: "+fechaLimite);
    long idUsuario=1000L;
    tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",fechaLimite, null);
    assertEquals(3,tareaService.allTareasUsuario(1000L).size());
  }

  // Test: modificación descripcion en Tarea
  @Test
  public void modificacionDescripcionTarea(){
    TareaService tareaService=newTareaService();
    Date fecha;
    long idTarea=1000L;
    Tarea tarea=tareaService.obtenerTarea(idTarea);
    tareaService.modificaTarea(idTarea,"Pagar el alquiler",null, "nueva Descripcion");
    tarea=tareaService.obtenerTarea(idTarea);
    assertEquals("nueva Descripcion",tarea.getDescripcion());
  }

  // Test: modificación descripcion en Tarea null
  @Test
  public void modificacionDescripcionTareaNull(){
    TareaService tareaService=newTareaService();
    Date fecha;
    long idTarea=1000L;
    Tarea tarea=tareaService.obtenerTarea(idTarea);
    tareaService.modificaTarea(idTarea,"Pagar el alquiler",null, null);
    tarea=tareaService.obtenerTarea(idTarea);
    assertEquals("",tarea.getDescripcion());
  }

  @Test
  public void listarTareasSinTablero() {
    TareaService tareaService = newTareaService();
    TableroService tableroService = newTableroService();
    long idUsuario=1000L;
    long idTablero=1000L;
    List<Tarea> tareas = tareaService.allTareasUsuario(idUsuario);
    assertEquals(2, tareas.size());
    Tarea tarea=tareaService.nuevaTarea(idUsuario,"Pagar las medicinas jeje",null,null);
    long idTarea=tarea.getId();
    tableroService.addTareaTablero(idTablero,idTarea);
    tareas = tareaService.allTareasUsuario(idUsuario);
    assertEquals(2, tareas.size());
  }

  @Test
  public void anadeEtiquetasVariasTareasPerteneceTablero(){
    TableroService tableroService=newTableroService();
    EtiquetaService etiquetaService=newEtiquetaService();
    TareaService tareaService=newTareaService();

    Long idTablero=1000L;
    Long idUsuario=1000L;

    Tablero tablero=tableroService.findTableroPorId(idTablero);
    Etiqueta etiqueta1=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
    Etiqueta etiqueta2=etiquetaService.creaEtiqueta("#000000","testEspecial2");
    Tarea tarea1=tareaService.nuevaTarea(idUsuario,"Titulo tarea1",null,null);
    Tarea tarea2=tareaService.nuevaTarea(idUsuario,"Titulo tarea2",null,null);

    tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta1.getId());
    tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta2.getId());
    tablero=tableroService.addTareaTablero(tablero.getId(),tarea1.getId());
    tablero=tableroService.addTareaTablero(tablero.getId(),tarea2.getId());

    tarea1=tareaService.addEtiquetaATarea(tarea1.getId(),etiqueta1.getId());
    tarea1=tareaService.addEtiquetaATarea(tarea1.getId(),etiqueta2.getId());
    tarea2=tareaService.addEtiquetaATarea(tarea2.getId(),etiqueta1.getId());

    assertTrue(tarea1.getEtiquetas().contains(etiqueta1));
    assertTrue(tarea1.getEtiquetas().contains(etiqueta2));
    assertTrue(tarea2.getEtiquetas().contains(etiqueta1));
    assertTrue(!(tarea2.getEtiquetas().contains(etiqueta2)));
  }

  @Test(expected=TareaServiceException.class)
  public void anadeEtiquetaATareaNoPerteneceTableroExcepcion(){
    TableroService tableroService=newTableroService();
    EtiquetaService etiquetaService=newEtiquetaService();
    TareaService tareaService=newTareaService();

    Long idTablero=1000L;
    Long idUsuario=1000L;

    Tablero tablero=tableroService.findTableroPorId(idTablero);
    Etiqueta etiqueta1=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
    Etiqueta etiqueta2=etiquetaService.creaEtiqueta("#000000","testEspecial2");
    Tarea tarea1=tareaService.nuevaTarea(idUsuario,"Titulo tarea1",null,null);
    Tarea tarea2=tareaService.nuevaTarea(idUsuario,"Titulo tarea2",null,null);

    tablero=tableroService.addTareaTablero(tablero.getId(),tarea1.getId());
    tablero=tableroService.addTareaTablero(tablero.getId(),tarea2.getId());

    tarea1=tareaService.addEtiquetaATarea(tarea1.getId(),etiqueta1.getId());
  }

  @Test
  public void anadeEtiquetasVariasTareasPerteneceUsuario(){
    UsuarioService usuarioService=newUsuarioService();
    EtiquetaService etiquetaService=newEtiquetaService();
    TareaService tareaService=newTareaService();

    Long idTablero=1000L;
    Long idUsuario=1000L;

    Usuario usuario=usuarioService.findUsuarioPorId(idUsuario);
    Etiqueta etiqueta1=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
    Etiqueta etiqueta2=etiquetaService.creaEtiqueta("#000000","testEspecial2");
    Tarea tarea1=tareaService.nuevaTarea(idUsuario,"Titulo tarea1",null,null);
    Tarea tarea2=tareaService.nuevaTarea(idUsuario,"Titulo tarea2",null,null);

    usuario=usuarioService.addEtiquetaAUsuario(usuario.getId(),etiqueta1.getId());
    usuario=usuarioService.addEtiquetaAUsuario(usuario.getId(),etiqueta2.getId());

    tarea1=tareaService.addEtiquetaATarea(tarea1.getId(),etiqueta1.getId());
    tarea1=tareaService.addEtiquetaATarea(tarea1.getId(),etiqueta2.getId());
    tarea2=tareaService.addEtiquetaATarea(tarea2.getId(),etiqueta1.getId());

    assertTrue(tarea1.getEtiquetas().contains(etiqueta1));
    assertTrue(tarea1.getEtiquetas().contains(etiqueta2));
    assertTrue(tarea2.getEtiquetas().contains(etiqueta1));
    assertTrue(!(tarea2.getEtiquetas().contains(etiqueta2)));
  }

  @Test(expected=TareaServiceException.class)
  public void anadeEtiquetasVariasTareasNoPerteneceUsuarioExcepcion(){
    UsuarioService usuarioService=newUsuarioService();
    EtiquetaService etiquetaService=newEtiquetaService();
    TareaService tareaService=newTareaService();

    Long idTablero=1000L;
    Long idUsuario=1000L;

    Usuario usuario=usuarioService.findUsuarioPorId(idUsuario);
    Etiqueta etiqueta1=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
    Etiqueta etiqueta2=etiquetaService.creaEtiqueta("#000000","testEspecial2");
    Tarea tarea1=tareaService.nuevaTarea(idUsuario,"Titulo tarea1",null,null);
    Tarea tarea2=tareaService.nuevaTarea(idUsuario,"Titulo tarea2",null,null);

    tarea1=tareaService.addEtiquetaATarea(tarea1.getId(),etiqueta1.getId());

  }

  @Test(expected=TareaServiceException.class)
  public void anadeEtiquetaNoExisteTareaExcepcion(){
    TareaService tareaService=newTareaService();
    EtiquetaService etiquetaService=newEtiquetaService();
    Long idTarea=1000L;
    Long idEtiqueta=10000L;
    Tarea tarea=tareaService.obtenerTarea(idTarea);
    tarea=tareaService.addEtiquetaATarea(tarea.getId(),idEtiqueta);
  }

  @Test(expected=TareaServiceException.class)
  public void anadeEtiquetaTareaNoExisteExcepcion(){
    TareaService tareaService=newTareaService();
    EtiquetaService etiquetaService=newEtiquetaService();
    Long idTarea=10000L;
    Etiqueta etiqueta=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
    tareaService.addEtiquetaATarea(idTarea,etiqueta.getId());
  }

    @Test
    public void borraEtiquetaTarea(){
      TableroService tableroService=newTableroService();
      EtiquetaService etiquetaService=newEtiquetaService();
      TareaService tareaService=newTareaService();
      Long idTablero=1000L;
      Long idTarea=1000L;
      Tablero tablero=tableroService.findTableroPorId(idTablero);
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      int numEtiquetas=tarea.getEtiquetas().size();
      Etiqueta etiqueta=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tablero=tableroService.addTareaTablero(tablero.getId(),tarea.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      int numEtiquetas2=tarea.getEtiquetas().size();
      assertTrue(numEtiquetas2>numEtiquetas);
      assertTrue(tarea.getEtiquetas().contains(etiqueta));
      tarea=tareaService.borraEtiquetaATarea(tarea.getId(),etiqueta.getId());
      int numEtiquetas3=tarea.getEtiquetas().size();
      assertEquals(numEtiquetas,numEtiquetas3);
      assertTrue(!(tarea.getEtiquetas().contains(etiqueta)));
    }

    @Test(expected=TareaServiceException.class)
    public void borraEtiquetaNoExistenteTareaExcepcion(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTarea=1000L;
      Long idEtiqueta=10000L;
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      tarea=tareaService.borraEtiquetaATarea(tarea.getId(),idEtiqueta);
    }

    @Test(expected=TareaServiceException.class)
    public void borraEtiquetaTareaNoExisteExcepcion(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTarea=10000L;
      Etiqueta etiqueta=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
      tareaService.borraEtiquetaATarea(idTarea,etiqueta.getId());
    }

    @Test(expected=TareaServiceException.class)
    public void borraEtiquetaNoPerteneceTareaExcepcion(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTarea=1000L;
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
      tarea=tareaService.borraEtiquetaATarea(idTarea,etiqueta.getId());
    }

    @Test
    public void etiquetaExisteTarea(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      TableroService tableroService=newTableroService();
      Long idTablero=1000L;
      Long idTarea=1000L;
      Tablero tablero=tableroService.findTableroPorId(idTablero);
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tablero=tableroService.addTareaTablero(tablero.getId(),tarea.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      assertTrue(tareaService.EtiquetaPerteneceTarea(idTarea,etiqueta.getColor(),etiqueta.getNombre()));
    }

    @Test
    public void etiquetaNoExisteTarea(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTarea=1000L;
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta("#ffffff","testEspecial");
      assertTrue(!(tareaService.EtiquetaPerteneceTarea(idTarea,etiqueta.getColor(),etiqueta.getNombre())));
    }

    @Test
    public void modificaEtiquetaTarea(){
      TareaService tareaService=newTareaService();
      TableroService tableroService=newTableroService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTablero=1000L;
      Long idTarea=1000L;
      String color="#ffffff";
      String nombre="testEspecial";
      String nuevoColor="#000000";
      String nuevoNombre="testEspecial2";
      Tablero tablero=tableroService.findTableroPorId(idTablero);
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta(color,nombre);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tablero=tableroService.addTareaTablero(tablero.getId(),tarea.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      assertTrue(!(tareaService.EtiquetaPerteneceTarea(idTarea,nuevoColor,nuevoNombre)));
      assertTrue(tareaService.EtiquetaPerteneceTarea(idTarea,color,nombre));

      tarea=tareaService.modificaEtiquetaATarea(tarea.getId(),etiqueta.getId(),nuevoColor,nuevoNombre);
      assertTrue(!(tareaService.EtiquetaPerteneceTarea(idTarea,color,nombre)));
      assertTrue(tareaService.EtiquetaPerteneceTarea(idTarea,nuevoColor,nuevoNombre));
      //Comprobamos que al cambiar los valores de la etiqueta, también se ha actualizado desde la referencia hacia tablero
      assertTrue(!(tableroService.EtiquetaPerteneceTablero(idTablero,color,nombre)));
      assertTrue(tableroService.EtiquetaPerteneceTablero(idTablero,nuevoColor,nuevoNombre));
    }

    @Test(expected=TareaServiceException.class)
    public void modificaEtiquetaTareaNoExistenteExcepcion(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTarea=1000L;
      String color="#ffffff";
      String nombre="testEspecial";
      String nuevoColor="#000000";
      String nuevoNombre="testEspecial2";
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta(color,nombre);
      tarea=tareaService.modificaEtiquetaATarea(10000L,etiqueta.getId(),nuevoColor,nuevoNombre);
    }

    @Test(expected=TareaServiceException.class)
    public void modificaEtiquetaNoExistenteTarea(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTarea=1000L;
      String color="#ffffff";
      String nombre="testEspecial";
      String nuevoColor="#000000";
      String nuevoNombre="testEspecial2";
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta(color,nombre);
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      tarea=tareaService.modificaEtiquetaATarea(tarea.getId(),10000L,nuevoColor,nuevoNombre);
    }

    @Test(expected=TareaServiceException.class)
    public void modificaEtiquetaTableroColorIncorrectoExcepcion(){
      TareaService tareaService=newTareaService();
      EtiquetaService etiquetaService=newEtiquetaService();
      Long idTarea=1000L;
      String color="#ffffff";
      String nombre="testEspecial";
      String nuevoColor="ffffff";
      String nuevoNombre="testEspecial2";
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta(color,nombre);
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      tarea=tareaService.modificaEtiquetaATarea(tarea.getId(),etiqueta.getId(),nuevoColor,nuevoNombre);
    }

    @Test
    public void listaEtiquetasTarea(){
      TableroService tableroService=newTableroService();
      EtiquetaService etiquetaService=newEtiquetaService();
      TareaService tareaService=newTareaService();
      Long idTablero=1000L;
      Long idTarea=1000L;
      String color1="#ffffff";
      String nombre1="testEspecial";
      String color2="#000000";
      String nombre2="testEspecial2";
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Tablero tablero=tableroService.findTableroPorId(idTablero);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta(color1,nombre1);
      tablero=tableroService.addTareaTablero(tablero.getId(),tarea.getId());
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      etiqueta=etiquetaService.creaEtiqueta(color1,null);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      etiqueta=etiquetaService.creaEtiqueta(color2,nombre1);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      etiqueta=etiquetaService.creaEtiqueta(color2,nombre2);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      etiqueta=etiquetaService.creaEtiqueta(color1,nombre2);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      etiqueta=etiquetaService.creaEtiqueta(color2,null);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta.getId());
      List<Etiqueta> etiquetas=tareaService.allEtiquetasTarea(idTarea);
      assertEquals(etiquetas.get(0).getColor(),color2);
      assertEquals(etiquetas.get(1).getColor(),color2);
      assertEquals(etiquetas.get(2).getColor(),color2);
      assertEquals(etiquetas.get(3).getColor(),color1);
      assertEquals(etiquetas.get(4).getColor(),color1);
      assertEquals(etiquetas.get(5).getColor(),color1);
      assertEquals(etiquetas.get(0).getNombre(),"");
      assertEquals(etiquetas.get(1).getNombre(),nombre1);
      assertEquals(etiquetas.get(2).getNombre(),nombre2);
      assertEquals(etiquetas.get(3).getNombre(),"");
      assertEquals(etiquetas.get(4).getNombre(),nombre1);
      assertEquals(etiquetas.get(5).getNombre(),nombre2);
    }

    @Test
    public void listaEtiquetasDisponibleTarea(){
      TableroService tableroService=newTableroService();
      EtiquetaService etiquetaService=newEtiquetaService();
      TareaService tareaService=newTareaService();
      Long idTablero=1000L;
      Long idTarea=1000L;
      String color1="#ffffff";
      String nombre1="testEspecial";
      String color2="#000000";
      String nombre2="testEspecial2";
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Tablero tablero=tableroService.findTableroPorId(idTablero);
      Etiqueta etiqueta=etiquetaService.creaEtiqueta(color1,nombre1);
      Long idEtiqueta1=etiqueta.getId();
      tablero=tableroService.addTareaTablero(tablero.getId(),tarea.getId());
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      etiqueta=etiquetaService.creaEtiqueta(color1,null);
      Long idEtiqueta2=etiqueta.getId();
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      etiqueta=etiquetaService.creaEtiqueta(color2,nombre1);
      Long idEtiqueta3=etiqueta.getId();
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta.getId());
      int libres1=tareaService.allEtiquetasTareaSinAsignarDisponibles(tarea.getId()).size();
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),idEtiqueta1);
      int libres2=tareaService.allEtiquetasTareaSinAsignarDisponibles(tarea.getId()).size();
      assertTrue(libres1>libres2);
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),idEtiqueta2);
      int libres3=tareaService.allEtiquetasTareaSinAsignarDisponibles(tarea.getId()).size();
      assertTrue(libres2>libres3);
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),idEtiqueta3);
      int libres4=tareaService.allEtiquetasTareaSinAsignarDisponibles(tarea.getId()).size();
      assertTrue(libres3>libres4);

    }

    @Test
    public void asignarUsuarioTarea(){
      TareaService tareaService=newTareaService();
      SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
      Date fecha=new Date();
      String fechaLimite=sdf.format(fecha).toString();
      long idUsuario=1000L;
      Tarea tarea=tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",fechaLimite, "descripcion");
      tarea=tareaService.addResponsableTarea(tarea.getId(), idUsuario);
      assertNotNull(tarea.getResponsable());
      assertEquals("Juan", tarea.getResponsable().getNombre());
    }

    @Test
    public void asignarUsuarioDiferenteAlQueCreaTarea(){
      TareaService tareaService=newTareaService();
      long idUsuario=1000L;
      long idUsuario2=1003L;
      Tarea tarea=tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",null, null);
      tarea=tareaService.addResponsableTarea(tarea.getId(), idUsuario2);
      assertNotNull(tarea.getResponsable());
      assertEquals("Adel", tarea.getResponsable().getNombre());
    }

    @Test
    public void asignarUsuarioTareaDeUnTablero(){
      TareaService tareaService=newTareaService();
      TableroService tableroService=newTableroService();
      long idUsuario=1000L;
      long idTablero=1000L;
      Tarea tarea=tareaService.nuevaTarea(idUsuario,"Do the right thing",null,null);
      tableroService.addTareaTablero(idTablero,tarea.getId());
      tarea=tareaService.addResponsableTarea(tarea.getId(), idUsuario);
      assertNotNull(tarea.getResponsable());
      assertEquals("Juan", tarea.getResponsable().getNombre());
    }

    @Test(expected = TareaServiceException.class)
    public void asignarUsuarioTareaNoExistente(){
      TareaService tareaService=newTareaService();
      long idUsuario=1000L;
      long idTarea= 4564L;
      tareaService.addResponsableTarea(idTarea, idUsuario);
    }

    @Test(expected = TareaServiceException.class)
    public void asignarUsuarioNoExistenteTarea(){
      TareaService tareaService=newTareaService();
      long idUsuario=9000L;
      long idTarea= 1000L;
      tareaService.addResponsableTarea(idTarea, idUsuario);
    }

    @Test
    public void borrarResponsableTarea(){
      TareaService tareaService=newTareaService();
      UsuarioService usuarioService=newUsuarioService();
      long idUsuario=1000L;
      long idTarea= 1000L;
      Tarea tarea=tareaService.addResponsableTarea(idTarea, idUsuario);
      assertNotNull(tarea.getResponsable());
      tarea=tareaService.borrarResponsableTarea(idTarea,idUsuario);
      Usuario user=usuarioService.findUsuarioPorId(idUsuario);
      assertFalse(user.getTareasAsig().contains(tarea));
      assertNull(tarea.getResponsable());
    }

    @Test
    public void borrarResponsableTareaDeTablero(){
      TareaService tareaService=newTareaService();
      UsuarioService usuarioService=newUsuarioService();
      TableroService tableroService=newTableroService();
      long idUsuario=1000L;
      long idTablero=1000L;
      Tarea tarea=tareaService.nuevaTarea(idUsuario,"Do the thing right",null,null);
      tableroService.addTareaTablero(idTablero,tarea.getId());
      tarea=tareaService.addResponsableTarea(tarea.getId(), idUsuario);
      assertNotNull(tarea.getResponsable());
      tarea=tareaService.borrarResponsableTarea(tarea.getId(),idUsuario);
      Usuario user=usuarioService.findUsuarioPorId(idUsuario);
      assertFalse(user.getTareasAsig().contains(tarea));
      assertNull(tarea.getResponsable());
    }

    @Test
    public void asignarBorrarAsignarUsuarioTarea(){
      TareaService tareaService=newTareaService();
      UsuarioService usuarioService=newUsuarioService();
      long idUsuario=1000L;
      long idUsuario2=1003L;
      Tarea tarea=tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",null, null);
      tarea=tareaService.addResponsableTarea(tarea.getId(), idUsuario);
      assertNotNull(tarea.getResponsable());
      assertEquals("Juan", tarea.getResponsable().getNombre());
      tarea=tareaService.borrarResponsableTarea(tarea.getId(),idUsuario);
      assertNull(tarea.getResponsable());
      tarea=tareaService.addResponsableTarea(tarea.getId(), idUsuario2);
      Usuario user=usuarioService.findUsuarioPorId(idUsuario2);
      assertTrue(user.getTareasAsig().contains(tarea));
      assertEquals("Adel", tarea.getResponsable().getNombre());
    }

    @Test()
    public void asignarUsuarioTareaTeniendoUno(){
      TareaService tareaService=newTareaService();
      UsuarioService usuarioService=newUsuarioService();
      long idUsuario=1000L;
      long idUsuario2=1003L;
      long idTarea= 1000L;
      Tarea tarea=tareaService.addResponsableTarea(idTarea, idUsuario);
      tarea=tareaService.addResponsableTarea(idTarea, idUsuario2);
      Usuario user=usuarioService.findUsuarioPorId(idUsuario2);
      assertEquals("Adel", tarea.getResponsable().getNombre());
      assertEquals(1,user.getTareasAsig().size());
      tarea=tareaService.addResponsableTarea(idTarea, idUsuario2);
      assertEquals(1,user.getTareasAsig().size());
      tarea=tareaService.addResponsableTarea(idTarea, idUsuario);
      assertEquals("Juan", tarea.getResponsable().getNombre());
    }

    @Test
    public void reactivarTareaTerminada(){
      TareaService tareaService=newTareaService();
      long idTarea =1000L;
      tareaService.tareaTerminada(idTarea);
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      assertTrue(tarea.getTerminada());
      tareaService.reactivarTareaTerminada(idTarea);
      tarea = tareaService.obtenerTarea(idTarea);
      assertFalse(tarea.getTerminada());
    }

    @Test(expected = TareaServiceException.class)
    public void reactivarTareaTerminadaNoExiste(){
      TareaService tareaService=newTareaService();
      long idTarea =10000L;
      tareaService.reactivarTareaTerminada(idTarea);
    }

    @Test
    public void listarTareasTablero(){
      TareaService tareaService=newTareaService();
      UsuarioService usuarioService=newUsuarioService();
      long idUsuario=1000L;
      long idTarea= 1000L;
      long idTarea2= 1002L;
      Tarea tarea=tareaService.addResponsableTarea(idTarea, idUsuario);
      tarea=tareaService.addResponsableTarea(idTarea2, idUsuario);
      Usuario user=usuarioService.findUsuarioPorId(idUsuario);
      List<Tarea> tareas= tareaService.allTareasResponsable(idUsuario);
      assertEquals("Juan", tarea.getResponsable().getNombre());
      assertEquals(2,tareas.size());
    }

    @Test
    public void crearYasignarTareasTablero(){
      TareaService tareaService=newTareaService();
      UsuarioService usuarioService=newUsuarioService();
      long idUsuario=1000L;
      long idTarea= 1000L;
      long idTarea2= 1002L;
      tareaService.nuevaTarea(idUsuario,"Pagar el alquiler",null, null);
      tareaService.nuevaTarea(idUsuario,"Pagar la boda",null, null);
      Tarea tarea=tareaService.addResponsableTarea(idTarea, idUsuario);
      tarea=tareaService.addResponsableTarea(idTarea2, idUsuario);
      Usuario user=usuarioService.findUsuarioPorId(idUsuario);
      List<Tarea> tareas= tareaService.allTareasResponsable(idUsuario);
      assertEquals("Juan", tarea.getResponsable().getNombre());
      assertEquals(2,tareas.size());
    }

    @Test
    public void filtrarTareasPorEtiquetasTablero(){
      TableroService tableroService=newTableroService();
      EtiquetaService etiquetaService=newEtiquetaService();
      TareaService tareaService=newTareaService();
      List <Etiqueta> etiquetasFiltrar=new ArrayList<Etiqueta>();
      List <Tarea> tareasFiltradas=new ArrayList<Tarea>();
      Long idTablero=1000L;
      Long idTarea=1000L;
      Long idTarea2=1001L;
      String color1="#ffffff";
      String nombre1="testEspecial";
      String color2="#000000";
      String nombre2="testEspecial2";
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Tarea tarea2=tareaService.obtenerTarea(idTarea2);
      Tablero tablero=tableroService.findTableroPorId(idTablero);
      Etiqueta etiqueta1=etiquetaService.creaEtiqueta(color1,nombre1);
      tablero=tableroService.addTareaTablero(tablero.getId(),tarea.getId());
      tablero=tableroService.addTareaTablero(tablero.getId(),tarea2.getId());
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta1.getId());
      Etiqueta etiqueta2=etiquetaService.creaEtiqueta(color1,null);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta2.getId());
      Etiqueta etiqueta3=etiquetaService.creaEtiqueta(color2,nombre1);
      tablero=tableroService.addEtiquetaATablero(tablero.getId(),etiqueta3.getId());
      tarea2=tareaService.addEtiquetaATarea(tarea2.getId(),etiqueta1.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta2.getId());
      etiquetasFiltrar.add(etiqueta3);
      tareasFiltradas=tareaService.filtradoTareas(idTablero,0L,etiquetasFiltrar);
      assertEquals(0,tareasFiltradas.size());
      etiquetasFiltrar.add(etiqueta2);
      tareasFiltradas=tareaService.filtradoTareas(idTablero,0L,etiquetasFiltrar);
      assertEquals(1,tareasFiltradas.size());
      etiquetasFiltrar.add(etiqueta1);
      tareasFiltradas=tareaService.filtradoTareas(idTablero,0L,etiquetasFiltrar);
      assertEquals(2,tareasFiltradas.size());
    }

    @Test
    public void filtrarTareasPorEtiquetasUsuario(){
      EtiquetaService etiquetaService=newEtiquetaService();
      TareaService tareaService=newTareaService();
      UsuarioService usuarioService=newUsuarioService();
      List <Etiqueta> etiquetasFiltrar=new ArrayList<Etiqueta>();
      List <Tarea> tareasFiltradas=new ArrayList<Tarea>();
      Long idTarea=1000L;
      Long idTarea2=1001L;
      Long idUsuario=1000L;
      String color1="#ffffff";
      String nombre1="testEspecial";
      String color2="#000000";
      String nombre2="testEspecial2";
      Tarea tarea=tareaService.obtenerTarea(idTarea);
      Tarea tarea2=tareaService.obtenerTarea(idTarea2);
      Etiqueta etiqueta1=etiquetaService.creaEtiqueta(color1,nombre1);
      usuarioService.addEtiquetaAUsuario(idUsuario,etiqueta1.getId());
      Etiqueta etiqueta2=etiquetaService.creaEtiqueta(color1,null);
      usuarioService.addEtiquetaAUsuario(idUsuario,etiqueta2.getId());
      Etiqueta etiqueta3=etiquetaService.creaEtiqueta(color2,nombre1);
      usuarioService.addEtiquetaAUsuario(idUsuario,etiqueta3.getId());
      tarea2=tareaService.addEtiquetaATarea(tarea2.getId(),etiqueta1.getId());
      tarea=tareaService.addEtiquetaATarea(tarea.getId(),etiqueta2.getId());
      etiquetasFiltrar.add(etiqueta3);
      tareasFiltradas=tareaService.filtradoTareas(0L,idUsuario,etiquetasFiltrar);
      assertEquals(0,tareasFiltradas.size());
      etiquetasFiltrar.add(etiqueta2);
      tareasFiltradas=tareaService.filtradoTareas(0L,idUsuario,etiquetasFiltrar);
      assertEquals(1,tareasFiltradas.size());
      etiquetasFiltrar.add(etiqueta1);
      tareasFiltradas=tareaService.filtradoTareas(0L,idUsuario,etiquetasFiltrar);
      assertEquals(2,tareasFiltradas.size());
    }

    @Test(expected=TareaServiceException.class)
    public void filtrarTareasPorEtiquetasNoExisteTableroExcepcion(){
      TareaService tareaService=newTareaService();
      List <Etiqueta> etiquetasFiltrar=new ArrayList<Etiqueta>();
      tareaService.filtradoTareas(10000L,0L,etiquetasFiltrar);
    }

    @Test(expected=TareaServiceException.class)
    public void filtrarTareasPorEtiquetasNoExisteUsuarioExcepcion(){
      TareaService tareaService=newTareaService();
      List <Etiqueta> etiquetasFiltrar=new ArrayList<Etiqueta>();
      tareaService.filtradoTareas(0L,10000L,etiquetasFiltrar);
    }
}
