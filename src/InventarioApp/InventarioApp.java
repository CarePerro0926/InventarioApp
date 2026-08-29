package InventarioApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class InventarioApp extends Application {
    private Stage loginStage;

    private void aplicarFondoResponsive(Region root, String rutaLocal) {
        String uri = new File(rutaLocal).toURI().toString();
        Image imagen = new Image(uri, true);

        BackgroundSize backgroundSize = new BackgroundSize(
            100, 100, true, true, false, true // ancho y alto en %, cover=true
        );

        BackgroundImage backgroundImage = new BackgroundImage(
            imagen,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            backgroundSize
        );

        root.setBackground(new Background(backgroundImage));
    }

    // Parámetros de estilo
    private static final String TOTAL_COLOR     = "red";
    private static final int    TOTAL_FONT_SIZE = 24;
    private static final String FONDO_PATH = "D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png";

    // Para el texto “Total: $” y el monto separado
    private Text totalPrefix;
    private Text totalValue;

    // Variables globales
    private UsuarioService usuarioService = new UsuarioService();

    // Método para establecer el ícono en cada ventana
    private void configurarIcono(Stage stage) {
        Image icono = new Image("file:/D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/images/logo.png");
        stage.getIcons().add(icono);
    }
    
    private StackPane crearFondoResponsive(Node contenido, String rutaLocal) {
        String uri = new File(rutaLocal).toURI().toString();
        ImageView fondo = new ImageView(new Image(uri));
        fondo.setPreserveRatio(false);
        fondo.setFitWidth(800);
        fondo.setFitHeight(600);
        fondo.setManaged(false);
        fondo.setMouseTransparent(true);

        StackPane stack = new StackPane(fondo, contenido);
        stack.widthProperty().addListener((obs, oldVal, newVal) -> fondo.setFitWidth(newVal.doubleValue()));
        stack.heightProperty().addListener((obs, oldVal, newVal) -> fondo.setFitHeight(newVal.doubleValue()));
        return stack;
    }
    
    private void mostrarAlertaAutoCierre(Alert.AlertType tipo, String mensaje, String iconoPath) {
        Alert alert = new Alert(tipo);
        alert.setContentText(mensaje);

        String uri = new File(FONDO_PATH).toURI().toString();
        Image img = new Image(uri, true);
        BackgroundSize bSize = new BackgroundSize(100, 100, true, true, false, true);
        BackgroundImage bgImg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bSize);

        DialogPane pane = alert.getDialogPane();
        pane.setBackground(new Background(bgImg));

        Stage stage = (Stage) pane.getScene().getWindow();
        configurarIcono(stage);

        alert.show();
        new Thread(() -> {
            try { Thread.sleep(900); } 
            catch (InterruptedException e) { /*ignore*/ }
            if (alert.isShowing()) {
                Platform.runLater(alert::close);
            }
        }).start();
    }

    // ================= MODELOS =================

    public static class Usuario {
        private String username;
        private String password;
        private String rol;

        public Usuario(String username, String password, String rol) {
            this.username = username;
            this.password = password;
            this.rol = rol;
        }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getRol() { return rol; }
    }

    public static class Producto {
        private String id;
        private String nombre;
        private int cantidad;
        private double precio;

        public Producto(String id, String nombre, int cantidad, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
        }

        @Override
        public String toString() {
            DecimalFormat formatoPesos = new DecimalFormat("$#,###");
            return "ID: " + id + ", Nombre: " + nombre + ", Cantidad: " + cantidad + ", Precio por unidad: " + formatoPesos.format(precio) + " COP";
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public int getCantidad() { return cantidad; }
        public double getPrecio() { return precio; }
    }

    public static class Proveedor {
        private String nombre;
        private String contacto;

        public Proveedor(String nombre, String contacto) {
            this.nombre = nombre;
            this.contacto = contacto;
        }

        @Override
        public String toString() {
            return "Nombre: " + nombre + ", Contacto: " + contacto;
        }

        public String getNombre() { return nombre; }
        public String getContacto() { return contacto; }
    }

    public static class UsuarioService {
        private List<Usuario> usuarios = new ArrayList<>();

        public UsuarioService() {
            usuarios.add(new Usuario("admin", "admin", "admin"));
            usuarios.add(new Usuario("cliente", "cliente", "cliente"));
        }

        public boolean validarUsuario(String username, String password) {
            for (Usuario usuario : usuarios) {
                if (usuario.getUsername().equals(username) && usuario.getPassword().equals(password)) {
                    return true;
                }
            }
            return false;
        }

        public String obtenerRol(String username) {
            for (Usuario usuario : usuarios) {
                if (usuario.getUsername().equals(username)) {
                    return usuario.getRol();
                }
            }
            return null;
        }
        public void agregarUsuario(Usuario nuevoUsuario) {
            usuarios.add(nuevoUsuario);
        }
    }

    // ================= SERVICIO CON MANEJO DE ARCHIVOS =================

    public static class InventarioService {
        private List<Producto> inventario = new ArrayList<>();
        private List<Proveedor> proveedores = new ArrayList<>();
        
        private static final String ARCHIVO_INVENTARIO = "inventario.txt";
        private static final String ARCHIVO_PROVEEDORES = "proveedores.txt";

        public InventarioService() {
            cargarDatosDesdeArchivo();
        }

        private void cargarDatosDesdeArchivo() {
            // 1. Cargar Inventario
            try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_INVENTARIO))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] datos = linea.split(",");
                    if (datos.length == 4) {
                        inventario.add(new Producto(datos[0], datos[1], Integer.parseInt(datos[2]), Double.parseDouble(datos[3])));
                    }
                }
            } catch (IOException e) {
                cargarDatosPorDefecto(); // Si no existe el archivo, carga los defaults y los guarda
            }

            // 2. Cargar Proveedores
            try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PROVEEDORES))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] datos = linea.split(",");
                    if (datos.length == 2) {
                        proveedores.add(new Proveedor(datos[0], datos[1]));
                    }
                }
            } catch (IOException e) {
                cargarProveedoresPorDefecto();
            }
        }

        private void guardarDatosEnArchivo() {
            // Guardar Inventario
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_INVENTARIO))) {
                for (Producto p : inventario) {
                    bw.write(p.getId() + "," + p.getNombre() + "," + p.getCantidad() + "," + p.getPrecio());
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Guardar Proveedores
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_PROVEEDORES))) {
                for (Proveedor prov : proveedores) {
                    bw.write(prov.getNombre() + "," + prov.getContacto());
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cargarDatosPorDefecto() {
            inventario.add(new Producto("1", "Disco Duro SSD 500GB", 50, 350000));
            inventario.add(new Producto("2", "Memoria RAM DDR4 8GB", 30, 150000));
            inventario.add(new Producto("3", "Procesador Intel Core i5", 20, 900000));
            inventario.add(new Producto("4", "Placa Madre ATX", 15, 650000));
            inventario.add(new Producto("5", "Fuente de Poder 650W", 25, 250000));
            inventario.add(new Producto("6", "Tarjeta Gráfica GTX 1660", 10, 1200000));
            inventario.add(new Producto("7", "Monitor LED 24 pulgadas", 20, 700000));
            inventario.add(new Producto("8", "Teclado Mecánico RGB", 40, 200000));
            inventario.add(new Producto("9", "Mouse Gamer", 35, 150000));
            inventario.add(new Producto("10", "Gabinete de PC con Ventiladores", 10, 450000));
            inventario.add(new Producto("11", "Disco Duro Externo 1TB", 25, 400000));
            inventario.add(new Producto("12", "Cámara Web Full HD", 30, 250000));
            guardarDatosEnArchivo();
        }

        private void cargarProveedoresPorDefecto() {
            proveedores.add(new Proveedor("Repuestos & Cía", "repuestos@cia.com"));
            proveedores.add(new Proveedor("TechParts", "ventas@techparts.com"));
            proveedores.add(new Proveedor("HardWare Pro", "contacto@hardwarepro.com"));
            guardarDatosEnArchivo();
        }

        // --- CRUD con persistencia ---
        public void agregarProducto(Producto producto) {
            inventario.add(producto);
            guardarDatosEnArchivo();
        }

        public void eliminarProducto(String id) {
            inventario.removeIf(producto -> producto.getId().equals(id));
            guardarDatosEnArchivo();
        }

        public List<Producto> obtenerInventario() {
            return inventario;
        }

        public void actualizarProducto(Producto productoActualizado) {
            for (Producto producto : inventario) {
                if (producto.getId().equals(productoActualizado.getId())) {
                    producto.cantidad = productoActualizado.getCantidad();
                    producto.precio = productoActualizado.getPrecio();
                    guardarDatosEnArchivo();
                    return;
                }
            }
        }

        public void agregarProveedor(Proveedor proveedor) {
            proveedores.add(proveedor);
            guardarDatosEnArchivo();
        }

        public List<Proveedor> obtenerProveedores() {
            return proveedores;
        }
    }

    // ================= LÓGICA DE LA APLICACIÓN =================

    private InventarioService inventarioService = new InventarioService();
    private List<Producto> carrito = new ArrayList<>();

    private List<Producto> filtrar(String texto) {
        String criterio = texto.toLowerCase().trim();
        return inventarioService.obtenerInventario().stream()
            .filter(p -> p.getId().toLowerCase().contains(criterio) || p.getNombre().toLowerCase().contains(criterio))
            .collect(Collectors.toList());
    }

    @Override
    public void start(Stage primaryStage) {
        this.loginStage = primaryStage;

        VBox loginRoot = new VBox(5);
        loginRoot.setPadding(new Insets(20, 10, 20, 10));
        loginRoot.setAlignment(Pos.CENTER);
        aplicarFondoResponsive(loginRoot, FONDO_PATH);

        Label loginLabel = new Label("Inicio de Sesión");
        loginLabel.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");

        Label stockSyncLabel = new Label("Stock-Sync");
        stockSyncLabel.setStyle("-fx-text-fill:red; -fx-font-size:20px; -fx-font-weight:bold;");

        VBox tituloBox = new VBox(2, loginLabel, stockSyncLabel);
        tituloBox.setAlignment(Pos.CENTER);

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario");
        txtUsuario.setStyle("-fx-background-color:white; -fx-border-color:gray; -fx-font-size:12px;");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setStyle("-fx-background-color:white; -fx-border-color:gray; -fx-font-size:12px;");

        HBox olvidasteBox = new HBox(new Label("¿Olvidaste la contraseña?"));
        olvidasteBox.setAlignment(Pos.CENTER_LEFT);
        olvidasteBox.getChildren().get(0).setStyle("-fx-text-fill:blue; -fx-underline:true; -fx-font-size:12px;");
        olvidasteBox.getChildren().get(0).setOnMouseClicked(e -> abrirVentanaOlvidarContrasena());

        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.setDefaultButton(true);
        btnLogin.setStyle("-fx-background-color:linear-gradient(#2e4053,#2e4053); -fx-background-radius:10; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:8 15; -fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.3),5,0,0,2);"); 
        
        btnLogin.setOnAction(e -> {
            String username = txtUsuario.getText();
            String password = txtPassword.getText();

            if (usuarioService.validarUsuario(username, password)) {
                String rol = usuarioService.obtenerRol(username);
                loginStage.hide();

                if ("admin".equals(rol)) {
                    abrirAdmin();
                    mostrarAlertaAutoCierre(Alert.AlertType.INFORMATION, "Inicio de sesión exitoso.", FONDO_PATH);
                } else {
                    abrirCliente();
                    mostrarAlertaAutoCierre(Alert.AlertType.INFORMATION, "Inicio de sesión exitoso.", FONDO_PATH);
                }
            } else {
                mostrarAlertaAutoCierre(Alert.AlertType.ERROR, "Credenciales inválidas. Intente nuevamente.", FONDO_PATH);
            }
        });

        Button btnRegistrarse = new Button("Registrarse");
        btnRegistrarse.setStyle(btnLogin.getStyle());
        btnRegistrarse.setOnAction(e -> abrirVentanaRegistro(usuarioService));

        HBox botonesBox = new HBox(5, btnLogin, btnRegistrarse);
        botonesBox.setAlignment(Pos.CENTER);

        Button btnCatalogo = new Button("Ver Catálogo");
        btnCatalogo.setOnAction(e -> abrirCatalogo(primaryStage));

        loginRoot.getChildren().addAll(tituloBox, txtUsuario, txtPassword, olvidasteBox, botonesBox, btnCatalogo);

        Scene loginScene = new Scene(loginRoot, 350, 280);
        primaryStage.setTitle("Sistema de Control de Inventario");
        configurarIcono(primaryStage);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Object abrirVentanaOlvidarContrasena() {
        Stage olvidarStage = new Stage();
        olvidarStage.setTitle("Recuperar Contraseña");
        configurarIcono(olvidarStage);

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        Label lblTitulo = new Label("Recuperar Contraseña");
        Label lblUsuario = new Label("Ingrese su usuario:");
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario");
        Button btnEnviar = new Button("Enviar");
        Button btnCancelar = new Button("Cancelar");

        root.getChildren().addAll(lblTitulo, lblUsuario, txtUsuario, btnEnviar, btnCancelar);

        Scene scene = new Scene(root, 400, 250);
        olvidarStage.setScene(scene);
        olvidarStage.show();

        btnEnviar.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Se han enviado instrucciones de recuperación a su email (simulado).");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            configurarIcono(alertStage);
            alert.show();
            olvidarStage.close();
        });

        btnCancelar.setOnAction(e -> olvidarStage.close());
        return null;
    }

    private void abrirVentanaRegistro(UsuarioService usuarioService) {
        Stage registroStage = new Stage();
        registroStage.setTitle("Registro de Nuevo Usuario");
        configurarIcono(registroStage);

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        TextField txtNombres = new TextField();
        txtNombres.setPromptText("Nombres");
        TextField txtApellidos = new TextField();
        txtApellidos.setPromptText("Apellidos");
        TextField txtCedula = new TextField();
        txtCedula.setPromptText("Cédula");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Fecha de Nacimiento");
        TextField txtCorreo = new TextField();
        txtCorreo.setPromptText("Correo Electrónico");
        TextField txtNombredeUsuario = new TextField();
        txtNombredeUsuario.setPromptText("Nombre de Usuario");

        Button btnGuardar = new Button("Registrar");
        Button btnCancelar = new Button("Cancelar");

        root.getChildren().addAll(new Label("Ingrese sus datos:"), txtNombres, txtApellidos, txtCedula, datePicker, txtCorreo, txtNombredeUsuario, btnGuardar, btnCancelar);

        Scene scene = new Scene(root, 400, 400);
        registroStage.setScene(scene);
        registroStage.show();
        
        btnGuardar.setOnAction(e -> {
            String nombres = txtNombres.getText();
            String apellidos = txtApellidos.getText();
            String cedula = txtCedula.getText();
            java.time.LocalDate fechaNacimiento = datePicker.getValue();
            String correo = txtCorreo.getText();
            String nombreUsuario = txtNombredeUsuario.getText();

            if (nombres.isEmpty() || apellidos.isEmpty() || cedula.isEmpty() || fechaNacimiento == null || correo.isEmpty() || nombreUsuario.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Por favor, complete todos los campos.");
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                configurarIcono(alertStage);
                alert.show();
            } else {
                usuarioService.agregarUsuario(new Usuario(nombreUsuario, "defaultPassword", "cliente"));
                mostrarAlertaAutoCierre(Alert.AlertType.INFORMATION, "Usuario registrado exitosamente.", "file:/D:/path/to/chequeo_icon.png");
                registroStage.close();
            }
        });

        btnCancelar.setOnAction(e -> registroStage.close());
    }
    
    private void abrirCatalogo(Stage parentStage) {
        Stage stage = new Stage();
        configurarIcono(stage);

        Label lblTitle = new Label("Catálogo de Productos");
        lblTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10); grid.setPadding(new Insets(10));
        String bold = "-fx-font-weight:bold;";
        Label hId = new Label("ID"); hId.setStyle(bold);
        Label hName = new Label("Nombre"); hName.setStyle(bold);
        Label hDisp = new Label("Cantidad disponible"); hDisp.setStyle(bold);
        Label hPrecio = new Label("Precio unidad"); hPrecio.setStyle(bold);
        
        GridPane.setHalignment(hId, HPos.LEFT);
        GridPane.setHalignment(hName, HPos.LEFT);
        GridPane.setHalignment(hDisp, HPos.CENTER);
        GridPane.setHalignment(hPrecio, HPos.RIGHT);
        grid.add(hId,0,0); grid.add(hName,1,0); grid.add(hDisp,2,0); grid.add(hPrecio,3,0);

        DecimalFormat df = new DecimalFormat("#,###");
        for(int row=1; row<=inventarioService.obtenerInventario().size(); row++){
            Producto p = inventarioService.obtenerInventario().get(row-1);
            Label cId = new Label("ID: "+p.getId()); cId.setTextFill(Color.RED); cId.setStyle(bold);
            Label cName = new Label(p.getNombre());
            Label cDisp = new Label(""+p.getCantidad()); GridPane.setHalignment(cDisp,HPos.CENTER);
            Label cPrecio = new Label(df.format(p.getPrecio())+" COP");
            grid.add(cId,0,row); grid.add(cName,1,row); grid.add(cDisp,2,row); grid.add(cPrecio,3,row);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Buscar ID o nombre...");
        txtSearch.setMaxWidth(300);
        txtSearch.textProperty().addListener((obs,ov,nv)->{
            List<Producto> list = filtrar(nv);
            grid.getChildren().clear();
            grid.add(hId,0,0); grid.add(hName,1,0); grid.add(hDisp,2,0); grid.add(hPrecio,3,0);
            int r=1;
            for(Producto p : list){
                Label iLbl = new Label("ID: "+p.getId()); iLbl.setTextFill(Color.RED); iLbl.setStyle(bold);
                Label nLbl = new Label(p.getNombre());
                Label dLbl = new Label(""+p.getCantidad()); GridPane.setHalignment(dLbl,HPos.CENTER);
                Label pLbl = new Label(df.format(p.getPrecio())+" COP");
                grid.add(iLbl,0,r); grid.add(nLbl,1,r); grid.add(dLbl,2,r); grid.add(pLbl,3,r);
                r++;
            }
        });

        Button btnReg = new Button("Regresar");
        btnReg.setOnAction(e->{ stage.close(); parentStage.show(); });

        VBox root = new VBox(10, lblTitle, txtSearch, scroll, btnReg);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root,600,400));
        stage.setTitle("Catálogo de Productos");
        stage.show();
    }

    private void abrirAdmin() {
        Stage adminStage = new Stage();
        configurarIcono(adminStage);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        aplicarFondoResponsive(root, "D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png");

        Label adminLabel = new Label("Menú de Administrador");
        adminLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        HBox topBox = new HBox(adminLabel);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.TOP_LEFT);

        Button btnConsultarInventario   = new Button("Consultar Inventario");
        Button btnAgregarProducto       = new Button("Agregar Producto");
        Button btnEliminarProducto      = new Button("Eliminar Producto");
        Button btnActualizarInventario  = new Button("Actualizar Inventario");
        Button btnActualizarProveedores = new Button("Actualizar Proveedores");
        Button btnConsultarProveedores  = new Button("Consultar Proveedores");

        btnConsultarInventario.setOnAction(e -> abrirVentanaConsultarInventario());
        btnAgregarProducto.setOnAction(e -> abrirVentanaAgregarProducto());
        btnEliminarProducto.setOnAction(e -> abrirVentanaEliminarProducto());
        btnActualizarInventario.setOnAction(e -> abrirVentanaActualizarInventario());
        btnActualizarProveedores.setOnAction(e -> abrirVentanaActualizarProveedores());
        btnConsultarProveedores.setOnAction(e -> abrirVentanaConsultarProveedores());

        centerBox.getChildren().addAll(btnConsultarInventario, btnAgregarProducto, btnEliminarProducto, btnActualizarInventario, btnActualizarProveedores, btnConsultarProveedores);
        root.setCenter(centerBox);

        Button btnLogout = new Button("Cerrar Sesión");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnLogout.setOnAction(e -> { adminStage.close(); loginStage.show(); });
        
        HBox bottomBox = new HBox(btnLogout);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(0, 0, 5, 0));
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 400, 400);
        adminStage.setScene(scene);
        adminStage.setTitle("Menú Administrador");
        adminStage.show();
    }

    private void abrirVentanaConsultarProveedores() {
        Stage proveedoresStage = new Stage();
        proveedoresStage.setTitle("Consultar Proveedores");
        configurarIcono(proveedoresStage);
        VBox proveedoresRoot = new VBox();
        proveedoresRoot.setSpacing(10);
        proveedoresRoot.setPadding(new Insets(10));
        
        Image imagenFondo = new Image("file:/D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png");
        BackgroundImage fondo = new BackgroundImage(imagenFondo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(400, 300, false, false, false, false));
        proveedoresRoot.setBackground(new Background(fondo));

        Label proveedoresLabel = new Label("Lista de Proveedores");
        TextArea proveedoresArea = new TextArea();
        proveedoresArea.setEditable(false);
        StringBuilder proveedoresString = new StringBuilder();

        for (Proveedor proveedor : inventarioService.obtenerProveedores()) {
            proveedoresString.append(proveedor.toString()).append("\n");
        }
        proveedoresArea.setText(proveedoresString.toString());
        Button btnCerrar = new Button("Cerrar");

        proveedoresRoot.getChildren().addAll(proveedoresLabel, proveedoresArea, btnCerrar);
        Scene proveedoresScene = new Scene(proveedoresRoot, 400, 300);
        proveedoresStage.setScene(proveedoresScene);
        proveedoresStage.show();
        btnCerrar.setOnAction(e -> proveedoresStage.close());
    }
    
    private void abrirVentanaConsultarInventario() {
        Stage inventarioStage = new Stage();
        inventarioStage.setTitle("Consultar Inventario");
        configurarIcono(inventarioStage);

        VBox inventarioRoot = new VBox(10);
        inventarioRoot.setPadding(new Insets(10));

        Label lblTitle = new Label("Inventario Disponible");
        lblTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10); grid.setPadding(new Insets(10));
        String bold = "-fx-font-weight:bold;";
        Label hId = new Label("ID"); hId.setStyle(bold);
        Label hName = new Label("Nombre"); hName.setStyle(bold);
        Label hDisp = new Label("Cantidad disponible"); hDisp.setStyle(bold);
        Label hPrecio = new Label("Precio unidad"); hPrecio.setStyle(bold);

        GridPane.setHalignment(hId, HPos.LEFT);
        GridPane.setHalignment(hName, HPos.LEFT);
        GridPane.setHalignment(hDisp, HPos.RIGHT);
        GridPane.setHalignment(hPrecio, HPos.RIGHT);
        grid.add(hId, 0, 0); grid.add(hName, 1, 0); grid.add(hDisp, 2, 0); grid.add(hPrecio, 3, 0);

        DecimalFormat df = new DecimalFormat("#,###");
        int row = 1;
        for (Producto p : inventarioService.obtenerInventario()) {
            Label lblId = new Label("ID: " + p.getId()); lblId.setTextFill(Color.RED); lblId.setStyle(bold);
            Label lblName = new Label(p.getNombre());
            Label lblDisp = new Label(String.valueOf(p.getCantidad())); GridPane.setHalignment(lblDisp, HPos.CENTER);
            Label lblPrecio = new Label(df.format(p.getPrecio()) + " COP");
            grid.add(lblId, 0, row); grid.add(lblName, 1, row); grid.add(lblDisp, 2, row); grid.add(lblPrecio, 3, row);
            row++;
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Buscar ID o nombre...");
        txtSearch.setMaxWidth(300);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Producto> filtrados = filtrar(newVal);
            grid.getChildren().clear();
            grid.add(hId, 0, 0); grid.add(hName, 1, 0); grid.add(hDisp, 2, 0); grid.add(hPrecio, 3, 0);
            int f = 1;
            for (Producto p : filtrados) {
                Label idLbl = new Label("ID: " + p.getId()); idLbl.setTextFill(Color.RED); idLbl.setStyle(bold);
                Label nameLbl = new Label(p.getNombre());
                Label dispLbl = new Label(String.valueOf(p.getCantidad())); GridPane.setHalignment(dispLbl, HPos.CENTER);
                Label priceLbl = new Label(df.format(p.getPrecio()) + " COP");
                grid.add(idLbl, 0, f); grid.add(nameLbl, 1, f); grid.add(dispLbl, 2, f); grid.add(priceLbl, 3, f);
                f++;
            }
        });

        Button btnRegresar = new Button("Regresar");
        btnRegresar.setOnAction(e -> inventarioStage.close());

        inventarioRoot.getChildren().setAll(lblTitle, txtSearch, scroll, btnRegresar);
        Scene inventarioScene = new Scene(crearFondoResponsive(inventarioRoot, FONDO_PATH), 600, 400);
        inventarioStage.setScene(inventarioScene);
        inventarioStage.show();
    }
    
    private void abrirVentanaAgregarProducto() {
        Stage agregarStage = new Stage();
        agregarStage.setTitle("Agregar Producto");
        configurarIcono(agregarStage);
        VBox agregarRoot = new VBox();
        agregarRoot.setSpacing(10);
        agregarRoot.setPadding(new Insets(10));
        
        Image imagenFondo = new Image("file:/D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png");
        BackgroundImage fondo = new BackgroundImage(imagenFondo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(300, 300, false, false, false, false));
        agregarRoot.setBackground(new Background(fondo));

        TextField txtId = new TextField(); txtId.setPromptText("ID del Producto");
        TextField txtNombre = new TextField(); txtNombre.setPromptText("Nombre del Producto");
        TextField txtCantidad = new TextField(); txtCantidad.setPromptText("Cantidad");
        TextField txtPrecio = new TextField(); txtPrecio.setPromptText("Precio");
        TextField txtProveedor = new TextField(); txtProveedor.setPromptText("Nombre del Proveedor");

        Button btnAgregar = new Button("Agregar Producto");
        agregarRoot.getChildren().addAll(txtId, txtNombre, txtCantidad, txtPrecio, txtProveedor, btnAgregar);

        btnAgregar.setOnAction(e -> {
            String id = txtId.getText();
            String nombre = txtNombre.getText();
            String cantText = txtCantidad.getText();
            String precText = txtPrecio.getText();
            String proveedor = txtProveedor.getText();

            try {
                int cantidad = Integer.parseInt(cantText);
                double precio = Double.parseDouble(precText);

                inventarioService.agregarProducto(new Producto(id, nombre, cantidad, precio));
                inventarioService.agregarProveedor(new Proveedor(proveedor, "Sin contacto"));

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setContentText("Producto agregado exitosamente.");
                Stage alertStage = (Stage) ok.getDialogPane().getScene().getWindow();
                configurarIcono(alertStage);
                ok.show();
                agregarStage.close();
            } catch (NumberFormatException ex) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Datos inválidos");
                error.setContentText("La cantidad y el precio deben ser números.\nVerifica e inténtalo de nuevo.");
                Stage alertStage = (Stage) error.getDialogPane().getScene().getWindow();
                configurarIcono(alertStage);
                error.show();
            }
        });

        Scene agregarScene = new Scene(agregarRoot, 300, 300);
        agregarStage.setScene(agregarScene);
        agregarStage.show();
    }

    private void abrirVentanaEliminarProducto() {
        Stage eliminarStage = new Stage();
        eliminarStage.setTitle("Eliminar Producto");
        configurarIcono(eliminarStage);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        aplicarFondoResponsive(root, "D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png");

        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar producto por ID o nombre…");
        txtBuscar.setMaxWidth(300);

        ListView<Producto> listView = new ListView<>();
        listView.getItems().setAll(inventarioService.obtenerInventario());

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            String criterio = newVal.toLowerCase().trim();
            List<Producto> filtrados = inventarioService.obtenerInventario().stream()
                .filter(p -> p.getId().toLowerCase().contains(criterio) || p.getNombre().toLowerCase().contains(criterio))
                .collect(Collectors.toList());
            listView.getItems().setAll(filtrados);
        });

        Button btnEliminar = new Button("Eliminar Producto");
        btnEliminar.setOnAction(e -> {
            Producto seleccionado = listView.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                Alert alerta = new Alert(Alert.AlertType.WARNING, "Primero selecciona un producto de la lista.");
                configurarIcono((Stage) alerta.getDialogPane().getScene().getWindow());
                alerta.show();
            } else {
                inventarioService.eliminarProducto(seleccionado.getId());
                listView.getItems().remove(seleccionado);
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Producto \"" + seleccionado.getNombre() + "\" eliminado.");
                configurarIcono((Stage) info.getDialogPane().getScene().getWindow());
                info.show();
            }
        });

        root.getChildren().addAll(new Label("Eliminar Producto"), txtBuscar, listView, btnEliminar);
        Scene scene = new Scene(root, 350, 400);
        eliminarStage.setScene(scene);
        eliminarStage.show();
    }

    private void abrirVentanaActualizarInventario() {
        Stage actualizarStage = new Stage();
        actualizarStage.setTitle("Actualizar Inventario");
        configurarIcono(actualizarStage);
        VBox actualizarRoot = new VBox();
        actualizarRoot.setSpacing(10);
        actualizarRoot.setPadding(new Insets(10));
        
        Image imagenFondo = new Image("file:/D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png");
        BackgroundImage fondo = new BackgroundImage(imagenFondo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(300, 250, false, false, false, false));
        actualizarRoot.setBackground(new Background(fondo));

        TextField txtId = new TextField(); txtId.setPromptText("ID del Producto a Actualizar");
        TextField txtCantidad = new TextField(); txtCantidad.setPromptText("Nueva Cantidad");
        TextField txtPrecio = new TextField(); txtPrecio.setPromptText("Nuevo Precio");

        Button btnActualizar = new Button("Actualizar Producto");
        actualizarRoot.getChildren().addAll(txtId, txtCantidad, txtPrecio, btnActualizar);

        btnActualizar.setOnAction(e -> {
            try {
                String id = txtId.getText();
                int cantidad = Integer.parseInt(txtCantidad.getText());
                double precio = Double.parseDouble(txtPrecio.getText());

                inventarioService.actualizarProducto(new Producto(id, "", cantidad, precio));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Producto actualizado exitosamente.");
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                configurarIcono(alertStage);
                alert.show();
                actualizarStage.close();
            } catch (NumberFormatException ex) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("La cantidad y el precio deben ser números.");
                error.show();
            }
        });

        Scene actualizarScene = new Scene(actualizarRoot, 300, 250);
        actualizarStage.setScene(actualizarScene);
        actualizarStage.show();
    }

    private void abrirVentanaActualizarProveedores() {
        Stage proveedoresStage = new Stage();
        proveedoresStage.setTitle("Actualizar Proveedores");
        configurarIcono(proveedoresStage);
        VBox proveedoresRoot = new VBox();
        proveedoresRoot.setSpacing(10);
        proveedoresRoot.setPadding(new Insets(10));
        
        Image imagenFondo = new Image("file:/D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png");
        BackgroundImage fondo = new BackgroundImage(imagenFondo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(300, 200, false, false, false, false));
        proveedoresRoot.setBackground(new Background(fondo));

        TextField txtNombre = new TextField(); txtNombre.setPromptText("Nombre del Proveedor");
        TextField txtContacto = new TextField(); txtContacto.setPromptText("Contacto del Proveedor");

        Button btnAgregarProveedor = new Button("Agregar Proveedor");
        proveedoresRoot.getChildren().addAll(txtNombre, txtContacto, btnAgregarProveedor);

        btnAgregarProveedor.setOnAction(e -> {
            String nombre = txtNombre.getText();
            String contacto = txtContacto.getText();

            inventarioService.agregarProveedor(new Proveedor(nombre, contacto));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Proveedor agregado exitosamente.");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            configurarIcono(alertStage);
            alert.show();
            proveedoresStage.close();
        });

        Scene proveedoresScene = new Scene(proveedoresRoot, 300, 200);
        proveedoresStage.setScene(proveedoresScene);
        proveedoresStage.show();
    }

    // ================= MODIFICACIÓN CLAVE: REFRESCO AUTOMÁTICO =================

    private void abrirCliente() {
        Stage stage = new Stage();
        configurarIcono(stage);

        totalPrefix = new Text("Total: $");
        totalPrefix.setFill(Color.web(TOTAL_COLOR));
        totalPrefix.setStyle("-fx-font-size: " + TOTAL_FONT_SIZE + "px; -fx-font-weight: bold;");

        totalValue = new Text("0 COP");
        totalValue.setFill(Color.BLACK);
        totalValue.setStyle("-fx-font-size: " + TOTAL_FONT_SIZE + "px; -fx-font-weight: bold;");

        TextFlow totalFlow = new TextFlow(totalPrefix, totalValue);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10); grid.setPadding(new Insets(10));
        String bold = "-fx-font-weight:bold;";
        Label hId = new Label("ID"); hId.setStyle(bold);
        Label hName = new Label("Nombre"); hName.setStyle(bold);
        Label hDisp = new Label("Disponible"); hDisp.setStyle(bold);
        Label hCant = new Label("Cantidad"); hCant.setStyle(bold);
        Label hPrecio = new Label("Precio unidad"); hPrecio.setStyle(bold);

        GridPane.setHalignment(hId, HPos.LEFT);
        GridPane.setHalignment(hName, HPos.CENTER);
        GridPane.setHalignment(hDisp, HPos.RIGHT);
        GridPane.setHalignment(hCant, HPos.RIGHT);
        GridPane.setHalignment(hPrecio, HPos.RIGHT);
        grid.add(hId, 0, 0); grid.add(hName, 1, 0); grid.add(hDisp, 2, 0); grid.add(hCant, 3, 0); grid.add(hPrecio, 4, 0);

        DecimalFormat df = new DecimalFormat("#,###");
        int row = 1;
        for (Producto p : inventarioService.obtenerInventario()) {
            Label lblId = new Label("ID: " + p.getId()); lblId.setTextFill(Color.RED); lblId.setStyle(bold);
            Label lblName = new Label(p.getNombre());
            Label lblDisp = new Label(String.valueOf(p.getCantidad())); GridPane.setHalignment(lblDisp, HPos.CENTER);

            TextField tfCantidad = new TextField();
            tfCantidad.setPromptText("Cantidad");
            tfCantidad.setPrefWidth(80);
            tfCantidad.textProperty().addListener((obs, oldV, newV) -> {
                carrito.removeIf(x -> x.getId().equals(p.getId()));
                try {
                    int c = Integer.parseInt(newV);
                    if (c > 0) {
                        carrito.add(new Producto(p.getId(), p.getNombre(), c, c * p.getPrecio()));
                    }
                } catch (NumberFormatException ex) { }
                actualizarTotal();
            });

            Label lblPrecio = new Label(df.format(p.getPrecio()) + " COP");
            grid.add(lblId, 0, row); grid.add(lblName, 1, row); grid.add(lblDisp, 2, row); grid.add(tfCantidad, 3, row); grid.add(lblPrecio, 4, row);
            row++;
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Buscar ID o nombre...");
        txtSearch.setMaxWidth(300);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Producto> filtrados = filtrar(newVal);
            grid.getChildren().clear();
            grid.add(hId, 0, 0); grid.add(hName, 1, 0); grid.add(hDisp, 2, 0); grid.add(hCant, 3, 0); grid.add(hPrecio, 4, 0);

            DecimalFormat df2 = new DecimalFormat("#,###");
            int f = 1;
            for (Producto p : filtrados) {
                Label idLbl = new Label("ID: " + p.getId()); idLbl.setTextFill(Color.RED); idLbl.setStyle(bold);
                Label nameLbl = new Label(p.getNombre());
                Label dispLbl = new Label(String.valueOf(p.getCantidad())); GridPane.setHalignment(dispLbl, HPos.CENTER);

                TextField tf = new TextField();
                tf.setPromptText("Cantidad"); tf.setPrefWidth(80);
                tf.textProperty().addListener((o, ov, nv) -> {
                    carrito.removeIf(x -> x.getId().equals(p.getId()));
                    try {
                        int c = Integer.parseInt(nv);
                        if (c > 0) {
                            carrito.add(new Producto(p.getId(), p.getNombre(), c, c * p.getPrecio()));
                        }
                    } catch (NumberFormatException ex) { }
                    actualizarTotal();
                });

                Label priceLbl = new Label(df2.format(p.getPrecio()) + " COP");
                grid.add(idLbl, 0, f); grid.add(nameLbl, 1, f); grid.add(dispLbl, 2, f); grid.add(tf, 3, f); grid.add(priceLbl, 4, f);
                f++;
            }
        });

        Button btnPagar = new Button("Pagar");
        // CAMBIO CLAVE: Le pasamos la ventana 'stage' al método de pago
        btnPagar.setOnAction(e -> abrirVentanaPago(stage)); 
        btnPagar.setStyle("-fx-background-color: linear-gradient(#2e4053,#2e4053); -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 15 8 15;");

        Button btnLogout = new Button("Cerrar Sesión");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnLogout.setOnAction(e -> { stage.close(); loginStage.show(); });

        HBox totalBox = new HBox(totalFlow); totalBox.setAlignment(Pos.CENTER_LEFT);
        HBox pagarBox = new HBox(btnPagar); pagarBox.setAlignment(Pos.CENTER_LEFT);
        HBox logoutBox = new HBox(btnLogout); logoutBox.setAlignment(Pos.CENTER_RIGHT);

        VBox bottomBox = new VBox(10, totalBox, pagarBox, logoutBox);
        bottomBox.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        VBox centerBox = new VBox(5, txtSearch, scroll);
        centerBox.setPadding(new Insets(10));

        root.setCenter(centerBox);
        root.setBottom(bottomBox);
        aplicarFondoResponsive(root, "D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png");

        Scene scene = new Scene(crearFondoResponsive(root, FONDO_PATH), 700, 500);
        stage.setTitle("Inventario Disponible");
        stage.setScene(scene);
        stage.show();
        actualizarTotal();
    }
    
    // CAMBIO CLAVE: Ahora recibe la ventana del cliente para poder reiniciarla
    private void abrirVentanaPago(Stage clientStage) {
        Stage pagoStage = new Stage();
        configurarIcono(pagoStage);
        VBox pagoRoot = new VBox();
        pagoRoot.setSpacing(10);
        pagoRoot.setPadding(new Insets(10));

        Label metodoPagoLabel = new Label("Seleccione el método de pago:");
        Button btnTarjeta = new Button("Tarjeta");
        Button btnEfecty = new Button("Consignación en Efecty");
        pagoRoot.getChildren().addAll(metodoPagoLabel, btnTarjeta, btnEfecty);

        // Acción al seleccionar pago con tarjeta
        btnTarjeta.setOnAction(e -> {
            boolean descuentoExitoso = descontarInventario();
            if (descuentoExitoso) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Pago con tarjeta seleccionado.\nGracias por su compra.\n\n¡Inventario actualizado!");
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                configurarIcono(alertStage);
                alert.show();
                
                carrito.clear();
                actualizarTotal();
                pagoStage.close();
                
                // MAGIA: Cerramos la vista vieja y abrimos una nueva con los datos frescos
                clientStage.close();
                abrirCliente(); 
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("No hay suficiente stock disponible.\nAlgunos productos no pueden ser vendidos.");
                error.show();
            }
        });

        // Acción al seleccionar pago con Efecty
        btnEfecty.setOnAction(e -> {
            boolean descuentoExitoso = descontarInventario();
            if (descuentoExitoso) {
                String codigoCompra = generarCodigoCompra();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Pago en Efecty seleccionado.\nPor favor use el siguiente código de compra:\n" + codigoCompra + "\n\n¡Inventario actualizado!");
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                configurarIcono(alertStage);
                alert.show();
                
                carrito.clear();
                actualizarTotal();
                pagoStage.close();
                
                // MAGIA: Cerramos la vista vieja y abrimos una nueva con los datos frescos
                clientStage.close();
                abrirCliente();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("No hay suficiente stock disponible.\nAlgunos productos no pueden ser vendidos.");
                error.show();
            }
        });

        Scene pagoScene = new Scene(pagoRoot, 300, 200);
        pagoStage.setTitle("Método de Pago");
        pagoStage.setScene(pagoScene);
        pagoStage.show();
    }

    private boolean descontarInventario() {
        // 1. Primero verificar si hay stock suficiente para TODA la compra
        for (Producto itemCarrito : carrito) {
            for (Producto productoInventario : inventarioService.obtenerInventario()) {
                if (productoInventario.getId().equals(itemCarrito.getId())) {
                    if (productoInventario.getCantidad() < itemCarrito.getCantidad()) {
                        return false; // No hay suficiente stock
                    }
                }
            }
        }
        
        // 2. Si llegamos aquí, hay stock suficiente. Ahora descontamos.
        for (Producto itemCarrito : carrito) {
            for (Producto productoInventario : inventarioService.obtenerInventario()) {
                if (productoInventario.getId().equals(itemCarrito.getId())) {
                    int nuevaCantidad = productoInventario.getCantidad() - itemCarrito.getCantidad();
                    
                    // Crear producto actualizado
                    Producto productoActualizado = new Producto(
                        productoInventario.getId(),
                        productoInventario.getNombre(),
                        nuevaCantidad,
                        productoInventario.getPrecio()
                    );
                    
                    // Actualizar en el inventario (esto también guarda automáticamente en el archivo .txt)
                    inventarioService.actualizarProducto(productoActualizado);
                    break;
                }
            }
        }
        
        return true;
    }

    private String generarCodigoCompra() {
        Random random = new Random();
        int codigo = random.nextInt(900000) + 100000;
        return String.valueOf(codigo);
    }

    private void actualizarTotal() {
        double total = carrito.stream().mapToDouble(Producto::getPrecio).sum();
        DecimalFormat formatoPesos = new DecimalFormat("#,###");
        totalValue.setText(formatoPesos.format(total) + " COP");
    }

    public static void main(String[] args) {
        launch(args);
    }
}