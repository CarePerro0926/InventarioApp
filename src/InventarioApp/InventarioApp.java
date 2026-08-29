package InventarioApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class InventarioApp extends Application {

    private Stage loginStage;
    private static final String TOTAL_COLOR = "#FF4500";
    private static final int TOTAL_FONT_SIZE = 24;
    private static final String FONDO_PATH = "D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/InventarioApp/images/inventario_fondo.png";

    private Text totalPrefix, totalValue;
    private UsuarioService usuarioService = new UsuarioService();
    private InventarioService inventarioService = new InventarioService();
    private List<Producto> carrito = new ArrayList<>();

    // ================= MODELOS =================

    public static class Usuario {
        private String username, password, rol;
        private String nombres, apellidos, cedula, email;

        public Usuario(String username, String password, String rol) {
            this.username = username; this.password = password; this.rol = rol;
        }

        public Usuario(String username, String password, String rol,
                       String nombres, String apellidos, String cedula, String email) {
            this(username, password, rol);
            this.nombres = nombres; this.apellidos = apellidos;
            this.cedula = cedula; this.email = email;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getRol() { return rol; }
        public String getEmail() { return email; }
    }

    public static class Producto {
        private String id, nombre, categoria;
        private int cantidad;
        private double precio;

        public Producto(String id, String nombre, String categoria, int cantidad, double precio) {
            this.id = id; this.nombre = nombre; this.categoria = categoria;
            this.cantidad = cantidad; this.precio = precio;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public int getCantidad() { return cantidad; }
        public double getPrecio() { return precio; }

        @Override
        public String toString() {
            return "ID: " + id + " | " + nombre + " [" + categoria + "] | Cant: " + cantidad;
        }
    }

    public static class Proveedor {
        private String nombre, contacto;
        public Proveedor(String nombre, String contacto) {
            this.nombre = nombre; this.contacto = contacto;
        }
        public String getNombre() { return nombre; }
        public String getContacto() { return contacto; }
        @Override
        public String toString() { return nombre + " – " + contacto; }
    }

    // ================= SERVICIOS =================

    public static class UsuarioService {
        private List<Usuario> usuarios = new ArrayList<>();
        private static final String ARCHIVO = "usuarios.txt";

        public UsuarioService() { cargar(); }

        private void cargar() {
            try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] d = linea.split(",");
                    if (d.length >= 3) usuarios.add(new Usuario(d[0], d[1], d[2]));
                }
            } catch (IOException e) {
                usuarios.add(new Usuario("admin", "admin", "admin"));
                usuarios.add(new Usuario("cliente", "cliente", "cliente"));
                guardar();
            }
        }

        private void guardar() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
                for (Usuario u : usuarios) {
                    bw.write(u.getUsername() + "," + u.getPassword() + "," + u.getRol());
                    bw.newLine();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

        public Usuario validar(String user, String pass) {
            for (Usuario u : usuarios)
                if (u.getUsername().equalsIgnoreCase(user.trim()) && u.getPassword().equals(pass.trim()))
                    return u;
            return null;
        }

        public boolean existeUsuario(String user) {
            return usuarios.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(user.trim()));
        }

        public void registrar(Usuario u) {
            if (!existeUsuario(u.getUsername())) {
                usuarios.add(u);
                guardar();
            }
        }
    }

    public static class InventarioService {
        private List<Producto> inventario = new ArrayList<>();
        private List<Proveedor> proveedores = new ArrayList<>();
        private static final String ARCH_INV = "inventario.txt";
        private static final String ARCH_PROV = "proveedores.txt";

        public InventarioService() { cargar(); }

        private void cargar() {
            try (BufferedReader br = new BufferedReader(new FileReader(ARCH_INV))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] d = linea.split(",");
                    if (d.length == 5)
                        inventario.add(new Producto(d[0], d[1], d[2],
                            Integer.parseInt(d[3]), Double.parseDouble(d[4])));
                }
            } catch (IOException e) { cargarPorDefecto(); }

            try (BufferedReader br = new BufferedReader(new FileReader(ARCH_PROV))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] d = linea.split(",");
                    if (d.length == 2) proveedores.add(new Proveedor(d[0], d[1]));
                }
            } catch (IOException e) { cargarProveedoresDefecto(); }
        }

        private void guardarInventario() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCH_INV))) {
                for (Producto p : inventario) {
                    bw.write(p.getId() + "," + p.getNombre() + "," + p.getCategoria()
                        + "," + p.getCantidad() + "," + p.getPrecio());
                    bw.newLine();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

        private void guardarProveedores() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCH_PROV))) {
                for (Proveedor p : proveedores) {
                    bw.write(p.getNombre() + "," + p.getContacto());
                    bw.newLine();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

        private void cargarPorDefecto() {
            inventario.add(new Producto("1","Procesador Intel Core i5-13400","Procesadores",12,1100000));
            inventario.add(new Producto("2","Tarjeta Gráfica RTX 4060","Tarjetas Gráficas",15,1600000));
            inventario.add(new Producto("3","Memoria RAM DDR5 16GB (3200MHz)","Memorias RAM",20,320000));
            inventario.add(new Producto("4","SSD SATA 1TB","Discos Duros",25,280000));
            inventario.add(new Producto("5","Placa Madre B660 Micro ATX","Boards",10,750000));
            inventario.add(new Producto("6","Fuente de Poder 650W 80+ Bronze","Fuentes de Poder",18,250000));
            inventario.add(new Producto("7","Gabinete Gamer con Ventiladores","Gabinetes",22,350000));
            inventario.add(new Producto("8","Teclado Mecánico RGB","Periféricos",30,220000));
            inventario.add(new Producto("9","Mouse Gaming Óptico","Periféricos",40,120000));
            inventario.add(new Producto("10","Audífonos Gaming Inalámbricos","Periféricos",16,380000));
            inventario.add(new Producto("11","Monitor LED 24\" 144Hz","Monitores",14,850000));
            inventario.add(new Producto("12","Cooler Air CPU Dual Tower","Refrigeración",18,180000));
            inventario.add(new Producto("13","Router Wi-Fi 6","Redes",18,320000));
            inventario.add(new Producto("14","Switch Ethernet 8 Puertos","Redes",12,180000));
            inventario.add(new Producto("15","Disipador Líquido RGB","Refrigeración",8,350000));
            inventario.add(new Producto("16","Kit de Ventiladores ARGB","Refrigeración",20,120000));
            inventario.add(new Producto("17","SSD NVMe 1TB","Discos Duros",22,480000));
            inventario.add(new Producto("18","Memoria RAM DDR5 16GB","Memorias RAM",16,320000));
            inventario.add(new Producto("19","Procesador AMD Ryzen 7","Procesadores",10,1100000));
            inventario.add(new Producto("20","Placa Madre Micro ATX","Boards",14,580000));
            inventario.add(new Producto("21","Fuente Modular 750W","Fuentes de Poder",10,300000));
            inventario.add(new Producto("22","Tarjeta Gráfica RTX 3060","Tarjetas Gráficas",6,1800000));
            inventario.add(new Producto("23","Monitor Curvo 27 pulgadas","Monitores",12,950000));
            inventario.add(new Producto("24","Combo Teclado y Mouse Inalámbrico","Periféricos",25,180000));
            inventario.add(new Producto("25","Hub USB 3.0","Periféricos",30,60000));
            inventario.add(new Producto("26","Soporte para Monitor","Accesorios",15,85000));
            inventario.add(new Producto("27","Cable HDMI 2.1","Accesorios",50,40000));
            inventario.add(new Producto("28","Micrófono Condensador USB","Periféricos",18,220000));
            inventario.add(new Producto("29","Silla Ergonómica Gamer","Mobiliario",10,750000));
            inventario.add(new Producto("30","Alfombrilla RGB XL","Accesorios",40,90000));
            guardarInventario();
        }

        private void cargarProveedoresDefecto() {
            proveedores.add(new Proveedor("NetZone","contacto@netzone.com"));
            proveedores.add(new Proveedor("CoolTech","ventas@cooltech.com"));
            proveedores.add(new Proveedor("DigitalStore","info@digitalstore.com"));
            proveedores.add(new Proveedor("PCMaster","soporte@pcmaster.com"));
            proveedores.add(new Proveedor("ElectroBits","ventas@electrobits.com"));
            proveedores.add(new Proveedor("GigaParts","giga@parts.com"));
            proveedores.add(new Proveedor("CompuWorld","ventas@compuworld.com"));
            guardarProveedores();
        }

        public List<Producto> obtenerInventario() { return inventario; }
        public List<Proveedor> obtenerProveedores() { return proveedores; }

        public void agregarProducto(Producto p) { inventario.add(p); guardarInventario(); }
        public void eliminarProducto(String id) {
            inventario.removeIf(x -> x.getId().equals(id));
            guardarInventario();
        }
        public void actualizarProducto(Producto p) {
            for (Producto x : inventario) {
                if (x.getId().equals(p.getId())) {
                    x.cantidad = p.getCantidad();
                    x.precio = p.getPrecio();
                    x.categoria = p.getCategoria();
                    guardarInventario();
                    return;
                }
            }
        }
        public void agregarProveedor(Proveedor p) { proveedores.add(p); guardarProveedores(); }

        public List<String> obtenerCategorias() {
            Set<String> cats = new LinkedHashSet<>();
            for (Producto p : inventario) cats.add(p.getCategoria());
            List<String> lista = new ArrayList<>();
            lista.add("Todas");
            lista.addAll(cats);
            return lista;
        }

        public List<Producto> filtrar(String texto, String categoria) {
            String txt = texto.toLowerCase().trim();
            return inventario.stream()
                .filter(p -> ("Todas".equals(categoria) || p.getCategoria().equals(categoria))
                    && (p.getId().toLowerCase().contains(txt)
                        || p.getNombre().toLowerCase().contains(txt)
                        || p.getCategoria().toLowerCase().contains(txt)))
                .collect(Collectors.toList());
        }
    }

    // ================= UTILIDADES VISUALES =================

    private void configurarIcono(Stage stage) {
        try {
            Image icono = new Image("file:/D:/ARCHIVOS UNIVERSIDAD/PROYECTOS ECLIPSE/InventarioApp/src/images/logo.png");
            stage.getIcons().add(icono);
        } catch (Exception e) {}
    }

    private void aplicarFondo(Region root) {
        try {
            String uri = new File(FONDO_PATH).toURI().toString();
            Image img = new Image(uri, true);
            BackgroundSize size = new BackgroundSize(100, 100, true, true, false, true);
            root.setBackground(new Background(new BackgroundImage(img,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, size)));
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #f0f0f0;");
        }
    }

    private StackPane crearFondo(Node contenido) {
        try {
            String uri = new File(FONDO_PATH).toURI().toString();
            ImageView fondo = new ImageView(new Image(uri));
            fondo.setPreserveRatio(false);
            fondo.setManaged(false);
            fondo.setMouseTransparent(true);
            StackPane stack = new StackPane(fondo, contenido);
            stack.widthProperty().addListener((o, ov, nv) -> fondo.setFitWidth(nv.doubleValue()));
            stack.heightProperty().addListener((o, ov, nv) -> fondo.setFitHeight(nv.doubleValue()));
            return stack;
        } catch (Exception e) {
            return new StackPane(contenido);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String msg) {
        Alert a = new Alert(tipo);
        a.setContentText(msg);
        configurarIcono((Stage) a.getDialogPane().getScene().getWindow());
        a.show();
        new Thread(() -> {
            try { Thread.sleep(900); } catch (InterruptedException e) {}
            Platform.runLater(() -> { if (a.isShowing()) a.close(); });
        }).start();
    }

    private DecimalFormat df() { return new DecimalFormat("#,###"); }

    // ================= INICIO =================

    @Override
    public void start(Stage primaryStage) {
        this.loginStage = primaryStage;
        mostrarLogin();
    }

    private void mostrarLogin() {
        Stage s = loginStage;
        s.setTitle("Stock-Sync - Inicio de Sesión");
        configurarIcono(s);

        VBox root = new VBox(10);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        aplicarFondo(root);

        Label titulo = new Label("Stock-Sync");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titulo.setTextFill(Color.web("#0F2C54"));

        TextField txtUser = new TextField(); txtUser.setPromptText("Usuario");
        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Contraseña");
        txtUser.setMaxWidth(280); txtPass.setMaxWidth(280);

        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.setDefaultButton(true);
        btnLogin.setStyle("-fx-background-color:#0F2C54; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 20;");
        
        Button btnReg = new Button("Registrarse");
        btnReg.setStyle("-fx-background-color:#0F2C54; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 20;");
        
        Button btnCat = new Button("Ver Catálogo");
        btnCat.setStyle("-fx-background-color:#6c7172; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 20;");

        HBox botones = new HBox(10, btnLogin, btnReg);
        botones.setAlignment(Pos.CENTER);

        Label olvide = new Label("¿Olvidaste tu contraseña?");
        olvide.setTextFill(Color.web("#17a2b8"));
        olvide.setUnderline(true);
        olvide.setStyle("-fx-cursor:hand;");

        btnLogin.setOnAction(e -> {
            Usuario u = usuarioService.validar(txtUser.getText(), txtPass.getText());
            if (u == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Usuario/clave inválidos");
            } else if ("admin".equals(u.getRol())) {
                s.hide();
                mostrarAdmin();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Bienvenido Administrador");
            } else {
                s.hide();
                mostrarCliente();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Bienvenido Cliente");
            }
        });
        btnReg.setOnAction(e -> { s.hide(); mostrarRegistro(); });
        btnCat.setOnAction(e -> { s.hide(); mostrarCatalogoPublico(s); });
        olvide.setOnMouseClicked(e -> mostrarRecuperar(s));

        root.getChildren().addAll(titulo, txtUser, txtPass, botones, btnCat, olvide);
        s.setScene(new Scene(crearFondo(root), 380, 380));
        s.show();
    }

    private void mostrarRegistro() {
        Stage s = new Stage();
        s.setTitle("Registro de Usuario");
        configurarIcono(s);

        VBox root = new VBox(8);
        root.setPadding(new Insets(20));
        aplicarFondo(root);

        Label titulo = new Label("Registro de Usuario");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TextField txtN = new TextField(); txtN.setPromptText("Nombres");
        TextField txtA = new TextField(); txtA.setPromptText("Apellidos");
        TextField txtC = new TextField(); txtC.setPromptText("Cédula");
        DatePicker dp = new DatePicker(); dp.setPromptText("Fecha de Nacimiento");
        TextField txtE = new TextField(); txtE.setPromptText("Correo Electrónico");
        TextField txtU = new TextField(); txtU.setPromptText("Nombre de Usuario");
        PasswordField txtP = new PasswordField(); txtP.setPromptText("Contraseña");
        ComboBox<String> cbRol = new ComboBox<>();
        cbRol.getItems().addAll("cliente", "admin");
        cbRol.setValue("cliente");

        Button btnReg = new Button("Registrar");
        btnReg.setDefaultButton(true);
        btnReg.setStyle("-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-weight:bold;");
        
        Button btnCan = new Button("Cancelar");

        btnReg.setOnAction(e -> {
            String n = txtN.getText().trim(), a = txtA.getText().trim(),
                   c = txtC.getText().trim(), em = txtE.getText().trim(),
                   u = txtU.getText().trim(), p = txtP.getText().trim();
            java.time.LocalDate f = dp.getValue();
            String rol = cbRol.getValue();

            if (n.isEmpty() || a.isEmpty() || c.isEmpty() || f == null
                || em.isEmpty() || u.isEmpty() || p.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Completa todos los campos");
                return;
            }
            if ("admin".equals(rol) && !em.toLowerCase().endsWith("@stocksync.com")) {
                mostrarAlerta(Alert.AlertType.ERROR, "Los administradores deben registrarse con un correo @stocksync.com");
                return;
            }
            if (usuarioService.existeUsuario(u)) {
                mostrarAlerta(Alert.AlertType.ERROR, "El usuario ya existe");
                return;
            }
            usuarioService.registrar(new Usuario(u, p, rol, n, a, c, em));
            mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario registrado correctamente");
            s.close();
            loginStage.show();
        });
        btnCan.setOnAction(e -> { s.close(); loginStage.show(); });

        root.getChildren().addAll(titulo, txtN, txtA, txtC, dp, txtE, txtU, txtP, cbRol, btnReg, btnCan);
        s.setScene(new Scene(crearFondo(root), 400, 520));
        s.show();
    }

    private void mostrarRecuperar(Stage parent) {
        Stage s = new Stage();
        s.setTitle("Recuperar Contraseña");
        configurarIcono(s);
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        Label t = new Label("Recuperar Contraseña");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        TextField txt = new TextField(); txt.setPromptText("Usuario o email");
        Button btn = new Button("Enviar");
        btn.setStyle("-fx-background-color:#0F2C54; -fx-text-fill:white;");
        Button can = new Button("Cerrar");
        btn.setOnAction(e -> {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Instrucciones enviadas a su correo");
            s.close();
        });
        can.setOnAction(e -> s.close());
        root.getChildren().addAll(t, txt, btn, can);
        s.setScene(new Scene(root, 350, 200));
        s.show();
    }

    private void mostrarCatalogoPublico(Stage parent) {
        Stage s = new Stage();
        s.setTitle("Catálogo Público");
        configurarIcono(s);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        aplicarFondo(root);

        Label t = new Label("Inventario Disponible");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        t.setTextFill(Color.web("#FF4500"));

        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll(inventarioService.obtenerCategorias());
        cbCat.setValue("Todas");
        TextField txt = new TextField(); txt.setPromptText("Buscar...");
        HBox filtros = new HBox(10, cbCat, txt);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(6);
        String bold = "-fx-font-weight:bold;";
        Label h1 = new Label("ID"); h1.setStyle(bold);
        Label h2 = new Label("Nombre"); h2.setStyle(bold);
        Label h3 = new Label("Categoría"); h3.setStyle(bold);
        Label h4 = new Label("Stock"); h4.setStyle(bold);
        Label h5 = new Label("Precio Unidad"); h5.setStyle(bold);
        grid.add(h1,0,0); grid.add(h2,1,0); grid.add(h3,2,0); grid.add(h4,3,0); grid.add(h5,4,0);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);

        Button btnBack = new Button("Regresar");
        btnBack.setStyle("-fx-background-color:#6c757d; -fx-text-fill:white;");
        btnBack.setOnAction(e -> { s.close(); parent.show(); });

        Runnable actualizar = () -> {
            grid.getChildren().removeIf(n -> GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) > 0);
            List<Producto> lista = inventarioService.filtrar(txt.getText(), cbCat.getValue());
            int r = 1;
            for (Producto p : lista) {
                Label id = new Label(p.getId());
                Label nom = new Label(p.getNombre());
                Label cat = new Label(p.getCategoria());
                Label stk = new Label(String.valueOf(p.getCantidad()));
                GridPane.setHalignment(stk, HPos.CENTER);
                Label pr = new Label(df().format(p.getPrecio()) + " COP");
                grid.add(id,0,r); grid.add(nom,1,r); grid.add(cat,2,r);
                grid.add(stk,3,r); grid.add(pr,4,r);
                r++;
            }
        };
        cbCat.setOnAction(e -> actualizar.run());
        txt.textProperty().addListener((o,ov,nv) -> actualizar.run());
        actualizar.run();

        root.getChildren().addAll(t, filtros, scroll, btnBack);
        s.setScene(new Scene(crearFondo(root), 700, 500));
        s.show();
    }

    private void mostrarCliente() {
        Stage s = new Stage();
        s.setTitle("Stock-Sync - Catálogo de Productos");
        configurarIcono(s);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        aplicarFondo(root);

        Label t = new Label("Catálogo de Productos");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll(inventarioService.obtenerCategorias());
        cbCat.setValue("Todas");
        TextField txt = new TextField(); txt.setPromptText("Buscar...");
        HBox filtros = new HBox(10, cbCat, txt);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(6);
        String bold = "-fx-font-weight:bold;";
        Label hId = new Label("ID"); hId.setStyle(bold);
        Label hNom = new Label("Nombre"); hNom.setStyle(bold);
        Label hCat = new Label("Categoría"); hCat.setStyle(bold);
        Label hStk = new Label("Stock"); hStk.setStyle(bold);
        Label hCant = new Label("Cantidad"); hCant.setStyle(bold);
        Label hPr = new Label("Precio Unidad"); hPr.setStyle(bold);
        grid.add(hId,0,0); grid.add(hNom,1,0); grid.add(hCat,2,0);
        grid.add(hStk,3,0); grid.add(hCant,4,0); grid.add(hPr,5,0);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);

        totalPrefix = new Text("Total: $");
        totalPrefix.setFill(Color.web(TOTAL_COLOR));
        totalPrefix.setFont(Font.font("Arial", FontWeight.BOLD, TOTAL_FONT_SIZE));
        totalValue = new Text("0 COP");
        totalValue.setFill(Color.BLACK);
        totalValue.setFont(Font.font("Arial", FontWeight.BOLD, TOTAL_FONT_SIZE));
        TextFlow totalFlow = new TextFlow(totalPrefix, totalValue);

        Button btnPay = new Button("Pagar");
        btnPay.setStyle("-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 20;");
        Button btnLogout = new Button("Cerrar Sesión");
        btnLogout.setStyle("-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-weight:bold;");

        HBox bottom = new HBox(20, totalFlow, btnPay, btnLogout);
        bottom.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(totalFlow, javafx.scene.layout.Priority.ALWAYS);
        HBox.setMargin(btnLogout, new Insets(0,0,0,10));

        Runnable render = () -> {
            grid.getChildren().removeIf(n -> GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) > 0);
            List<Producto> lista = inventarioService.filtrar(txt.getText(), cbCat.getValue());
            int r = 1;
            for (Producto p : lista) {
                Label id = new Label(p.getId());
                Label nom = new Label(p.getNombre());
                Label cat = new Label(p.getCategoria());
                Label stk = new Label(String.valueOf(p.getCantidad()));
                GridPane.setHalignment(stk, HPos.CENTER);

                TextField tf = new TextField();
                tf.setPromptText("0");
                tf.setPrefWidth(70);
                tf.setPrefHeight(25);

                tf.focusedProperty().addListener((obs,ov,nv) -> {
                    if (nv && "0".equals(tf.getText())) tf.clear();
                });
                tf.textProperty().addListener((obs,ov,nv) -> {
                    if ("0".equals(nv)) tf.clear();
                    carrito.removeIf(x -> x.getId().equals(p.getId()));
                    try {
                        int c = nv.isEmpty() ? 0 : Integer.parseInt(nv);
                        if (c > 0) carrito.add(new Producto(p.getId(), p.getNombre(), p.getCategoria(), c, c * p.getPrecio()));
                    } catch (NumberFormatException ex) {}
                    actualizarTotal();
                });

                Label pr = new Label(df().format(p.getPrecio()) + " COP");
                grid.add(id,0,r); grid.add(nom,1,r); grid.add(cat,2,r);
                grid.add(stk,3,r); grid.add(tf,4,r); grid.add(pr,5,r);
                r++;
            }
        };

        cbCat.setOnAction(e -> render.run());
        txt.textProperty().addListener((o,ov,nv) -> render.run());
        render.run();

        btnPay.setOnAction(e -> mostrarPago(s));
        btnLogout.setOnAction(e -> {
            carrito.clear();
            actualizarTotal();
            s.close();
            loginStage.show();
        });

        root.getChildren().addAll(t, filtros, scroll, bottom);
        s.setScene(new Scene(crearFondo(root), 800, 600));
        s.show();
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = carrito.stream().mapToDouble(Producto::getPrecio).sum();
        totalValue.setText(df().format(total) + " COP");
    }

    private void mostrarPago(Stage clientStage) {
        Stage s = new Stage();
        s.setTitle("Método de Pago");
        configurarIcono(s);

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Label t = new Label("Método de Pago");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        t.setTextFill(Color.web("#0F2C54"));

        Button btnCard = new Button("💳 Tarjeta de Crédito/Débito");
        btnCard.setStyle("-fx-background-color:#343a40; -fx-text-fill:white; -fx-font-size:14px; -fx-padding:12 20;");
        Button btnEfecty = new Button("🏦 Consignación en Efecty");
        btnEfecty.setStyle("-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-size:14px; -fx-padding:12 20;");

        btnCard.setMaxWidth(Double.MAX_VALUE);
        btnEfecty.setMaxWidth(Double.MAX_VALUE);

        btnCard.setOnAction(e -> { s.close(); mostrarPagoTarjeta(clientStage); });
        btnEfecty.setOnAction(e -> {
            if (carrito.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "El carrito está vacío");
                return;
            }
            if (!descontarInventario()) {
                mostrarAlerta(Alert.AlertType.ERROR, "No hay suficiente stock disponible");
                return;
            }
            String codigo = String.valueOf(new Random().nextInt(900000) + 100000);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Consignación en Efecty");
            a.setHeaderText("Guarde este código de consignación:");
            Label lbl = new Label(codigo);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 32));
            lbl.setTextFill(Color.web("#FF4500"));
            a.getDialogPane().setContent(lbl);
            configurarIcono((Stage) a.getDialogPane().getScene().getWindow());
            a.showAndWait();
            carrito.clear();
            actualizarTotal();
            s.close();
            clientStage.close();
            mostrarCliente();
        });

        root.getChildren().addAll(t, btnCard, btnEfecty);
        s.setScene(new Scene(root, 400, 250));
        s.show();
    }

    private void mostrarPagoTarjeta(Stage clientStage) {
        Stage s = new Stage();
        s.setTitle("Pago con Tarjeta");
        configurarIcono(s);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label t = new Label("Pago con Tarjeta");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ComboBox<String> cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll("Visa", "Mastercard", "American Express", "Discover", "Diners Club");
        cbTipo.setPromptText("Tipo de Tarjeta");

        // CORRECCIÓN: Uso de TextFormatter en lugar de setMaxLength (que no existe en JavaFX)
        TextField txtNum = new TextField();
        txtNum.setPromptText("Número de tarjeta");
        txtNum.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= 19) return change;
            return null; // Rechaza el cambio si supera 19 caracteres
        }));

        TextField txtTit = new TextField();
        txtTit.setPromptText("Nombre del titular");

        TextField txtExp = new TextField();
        txtExp.setPromptText("MM/AA");
        txtExp.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= 5) return change;
            return null; // Rechaza el cambio si supera 5 caracteres
        }));

        TextField txtCvv = new TextField();
        txtCvv.setPromptText("CVV");
        txtCvv.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= 4) return change;
            return null; // Rechaza el cambio si supera 4 caracteres
        }));

        Button btn = new Button("Confirmar Pago");
        btn.setStyle("-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10;");
        btn.setMaxWidth(Double.MAX_VALUE);

        btn.setOnAction(e -> {
            if (cbTipo.getValue() == null || txtNum.getText().trim().isEmpty()
                || txtTit.getText().trim().isEmpty() || txtExp.getText().trim().isEmpty()
                || txtCvv.getText().trim().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Complete todos los campos");
                return;
            }
            if (carrito.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "El carrito está vacío");
                return;
            }
            if (!descontarInventario()) {
                mostrarAlerta(Alert.AlertType.ERROR, "No hay suficiente stock disponible");
                return;
            }
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("¡Pago Exitoso!");
            a.setHeaderText("Gracias por su compra");
            a.setContentText("Su pago con " + cbTipo.getValue() + " ha sido procesado correctamente.");
            configurarIcono((Stage) a.getDialogPane().getScene().getWindow());
            a.showAndWait();
            carrito.clear();
            actualizarTotal();
            s.close();
            clientStage.close();
            mostrarCliente();
        });

        root.getChildren().addAll(t, cbTipo, txtNum, txtTit, txtExp, txtCvv, btn);
        s.setScene(new Scene(root, 380, 380));
        s.show();
    }

    private boolean descontarInventario() {
        for (Producto c : carrito) {
            for (Producto p : inventarioService.obtenerInventario()) {
                if (p.getId().equals(c.getId()) && p.getCantidad() < c.getCantidad())
                    return false;
            }
        }
        for (Producto c : carrito) {
            for (Producto p : inventarioService.obtenerInventario()) {
                if (p.getId().equals(c.getId())) {
                    int nueva = p.getCantidad() - c.getCantidad();
                    inventarioService.actualizarProducto(
                        new Producto(p.getId(), p.getNombre(), p.getCategoria(), nueva, p.getPrecio()));
                    break;
                }
            }
        }
        return true;
    }

    // ================= ADMIN =================

    private void mostrarAdmin() {
        Stage s = new Stage();
        s.setTitle("Stock-Sync - Panel Administrador");
        configurarIcono(s);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        aplicarFondo(root);

        Label t = new Label("Panel Administrador");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        t.setTextFill(Color.web("#0F2C54"));
        root.setTop(t);

        TabPane tabs = new TabPane();

        // TAB INVENTARIO
        Tab tabInv = new Tab("Inventario");
        tabInv.setClosable(false);
        VBox invBox = new VBox(10);
        invBox.setPadding(new Insets(10));

        ComboBox<String> cbCatA = new ComboBox<>();
        cbCatA.getItems().addAll(inventarioService.obtenerCategorias());
        cbCatA.setValue("Todas");
        TextField txtA = new TextField(); txtA.setPromptText("Buscar...");
        HBox filtA = new HBox(10, cbCatA, txtA);

        GridPane gridA = new GridPane();
        gridA.setHgap(10); gridA.setVgap(6);
        String bold = "-fx-font-weight:bold;";
        Label h1 = new Label("ID"); h1.setStyle(bold);
        Label h2 = new Label("Nombre"); h2.setStyle(bold);
        Label h3 = new Label("Categoría"); h3.setStyle(bold);
        Label h4 = new Label("Stock"); h4.setStyle(bold);
        Label h5 = new Label("Precio"); h5.setStyle(bold);
        gridA.add(h1,0,0); gridA.add(h2,1,0); gridA.add(h3,2,0);
        gridA.add(h4,3,0); gridA.add(h5,4,0);

        ScrollPane scrollA = new ScrollPane(gridA);
        scrollA.setFitToWidth(true);
        scrollA.setPrefHeight(300);

        Runnable updA = () -> {
            gridA.getChildren().removeIf(n -> GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) > 0);
            List<Producto> lista = inventarioService.filtrar(txtA.getText(), cbCatA.getValue());
            int r = 1;
            for (Producto p : lista) {
                gridA.add(new Label(p.getId()), 0, r);
                gridA.add(new Label(p.getNombre()), 1, r);
                gridA.add(new Label(p.getCategoria()), 2, r);
                Label stk = new Label(String.valueOf(p.getCantidad()));
                GridPane.setHalignment(stk, HPos.CENTER);
                gridA.add(stk, 3, r);
                gridA.add(new Label(df().format(p.getPrecio()) + " COP"), 4, r);
                r++;
            }
        };
        cbCatA.setOnAction(e -> updA.run());
        txtA.textProperty().addListener((o,ov,nv) -> updA.run());
        updA.run();

        invBox.getChildren().addAll(new Label("Inventario"), filtA, scrollA);
        tabInv.setContent(invBox);

        // TAB AGREGAR
        Tab tabAdd = new Tab("Agregar");
        tabAdd.setClosable(false);
        VBox addBox = new VBox(10);
        addBox.setPadding(new Insets(10));

        TextField addId = new TextField(); addId.setPromptText("ID");
        TextField addNom = new TextField(); addNom.setPromptText("Nombre");
        ComboBox<String> addCat = new ComboBox<>();
        addCat.setPromptText("Categoría");
        addCat.setEditable(true);
        addCat.getItems().addAll(inventarioService.obtenerCategorias().stream()
            .filter(c -> !"Todas".equals(c)).collect(Collectors.toList()));
        TextField addQty = new TextField(); addQty.setPromptText("Cantidad");
        TextField addPr = new TextField(); addPr.setPromptText("Precio");
        Button btnAdd = new Button("Agregar");
        btnAdd.setStyle("-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-weight:bold;");

        btnAdd.setOnAction(e -> {
            try {
                String id = addId.getText().trim(), nom = addNom.getText().trim(),
                       cat = addCat.getValue();
                int qty = Integer.parseInt(addQty.getText().trim());
                double pr = Double.parseDouble(addPr.getText().trim());
                if (id.isEmpty() || nom.isEmpty() || cat == null || cat.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Complete todos los campos");
                    return;
                }
                if (inventarioService.obtenerInventario().stream().anyMatch(p -> p.getId().equals(id))) {
                    mostrarAlerta(Alert.AlertType.ERROR, "El ID ya existe");
                    return;
                }
                inventarioService.agregarProducto(new Producto(id, nom, cat, qty, pr));
                mostrarAlerta(Alert.AlertType.INFORMATION, "Producto agregado y guardado");
                addId.clear(); addNom.clear(); addCat.setValue(null);
                addQty.clear(); addPr.clear();
                updA.run();
            } catch (NumberFormatException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Cantidad y precio deben ser números");
            }
        });
        addBox.getChildren().addAll(new Label("Agregar Producto"), addId, addNom, addCat, addQty, addPr, btnAdd);
        tabAdd.setContent(addBox);

        // TAB ACTUALIZAR
        Tab tabUpd = new Tab("Actualizar");
        tabUpd.setClosable(false);
        VBox updBox = new VBox(10);
        updBox.setPadding(new Insets(10));

        TextField updId = new TextField(); updId.setPromptText("ID del Producto a Actualizar (Obligatorio)");
        TextField updNom = new TextField(); updNom.setPromptText("Nuevo Nombre (Dejar en blanco para no cambiar)");
        ComboBox<String> updCat = new ComboBox<>();
        updCat.setPromptText("Nueva Categoría (Dejar en blanco para no cambiar)");
        updCat.setEditable(true);
        updCat.getItems().addAll(inventarioService.obtenerCategorias().stream()
            .filter(c -> !"Todas".equals(c)).collect(Collectors.toList()));
        TextField updQty = new TextField(); updQty.setPromptText("Nueva Cantidad (Dejar en blanco para no cambiar)");
        TextField updPr = new TextField(); updPr.setPromptText("Nuevo Precio (Dejar en blanco para no cambiar)");
        
        Button btnUpd = new Button("Actualizar Producto");
        btnUpd.setStyle("-fx-background-color:#007bff; -fx-text-fill:white; -fx-font-weight:bold;");

        btnUpd.setOnAction(e -> {
            String id = updId.getText().trim();
            if (id.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Debe ingresar el ID del producto que desea actualizar.");
                return;
            }

            Producto existente = inventarioService.obtenerInventario().stream()
                .filter(p -> p.getId().equals(id)).findFirst().orElse(null);

            if (existente == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "No se encontró ningún producto con el ID: " + id);
                return;
            }

            try {
                String nomFinal = updNom.getText().trim().isEmpty() ? existente.getNombre() : updNom.getText().trim();
                
                String catFinal = existente.getCategoria();
                if (updCat.getValue() != null && !updCat.getValue().trim().isEmpty()) {
                    catFinal = updCat.getValue().trim();
                }

                int qtyFinal = existente.getCantidad();
                if (!updQty.getText().trim().isEmpty()) {
                    qtyFinal = Integer.parseInt(updQty.getText().trim());
                }

                double prFinal = existente.getPrecio();
                if (!updPr.getText().trim().isEmpty()) {
                    prFinal = Double.parseDouble(updPr.getText().trim());
                }

                Producto productoActualizado = new Producto(id, nomFinal, catFinal, qtyFinal, prFinal);
                inventarioService.actualizarProducto(productoActualizado);

                mostrarAlerta(Alert.AlertType.INFORMATION, "Producto actualizado y guardado en inventario.txt correctamente.");
                
                updId.clear(); updNom.clear(); updCat.setValue(null);
                updQty.clear(); updPr.clear();
                updA.run();

            } catch (NumberFormatException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "La cantidad y el precio deben ser números válidos.");
            }
        });

        updBox.getChildren().addAll(new Label("Actualizar Producto"), updId, updNom, updCat, updQty, updPr, btnUpd);
        tabUpd.setContent(updBox);

        // TAB ELIMINAR
        Tab tabDel = new Tab("Eliminar");
        tabDel.setClosable(false);
        VBox delBox = new VBox(10);
        delBox.setPadding(new Insets(10));

        TextField delId = new TextField(); delId.setPromptText("ID a eliminar");
        Button btnDel = new Button("Eliminar");
        btnDel.setStyle("-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-weight:bold;");
        btnDel.setOnAction(e -> {
            String id = delId.getText().trim();
            if (id.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Ingrese un ID");
                return;
            }
            inventarioService.eliminarProducto(id);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Producto eliminado y guardado");
            delId.clear();
            updA.run();
        });
        delBox.getChildren().addAll(new Label("Eliminar Producto"), delId, btnDel);
        tabDel.setContent(delBox);

        // TAB PROVEEDORES
        Tab tabProv = new Tab("Proveedores");
        tabProv.setClosable(false);
        VBox provBox = new VBox(10);
        provBox.setPadding(new Insets(10));

        TextField provN = new TextField(); provN.setPromptText("Nombre Proveedor");
        TextField provE = new TextField(); provE.setPromptText("Email Proveedor");
        Button btnProv = new Button("Agregar");
        btnProv.setStyle("-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-weight:bold;");
        ListView<String> lstProv = new ListView<>();
        lstProv.setPrefHeight(200);

        Runnable updProv = () -> {
            lstProv.getItems().clear();
            for (Proveedor p : inventarioService.obtenerProveedores())
                lstProv.getItems().add(p.toString());
        };
        btnProv.setOnAction(e -> {
            String n = provN.getText().trim(), em = provE.getText().trim();
            if (n.isEmpty() || em.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Complete proveedor");
                return;
            }
            inventarioService.agregarProveedor(new Proveedor(n, em));
            mostrarAlerta(Alert.AlertType.INFORMATION, "Proveedor agregado y guardado");
            provN.clear(); provE.clear();
            updProv.run();
        });
        updProv.run();
        provBox.getChildren().addAll(new Label("Proveedores"), provN, provE, btnProv, lstProv);
        tabProv.setContent(provBox);

        tabs.getTabs().addAll(tabInv, tabAdd, tabUpd, tabDel, tabProv);
        root.setCenter(tabs);

        Button btnLogout = new Button("Cerrar Sesión");
        btnLogout.setStyle("-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-weight:bold;");
        btnLogout.setOnAction(e -> { s.close(); loginStage.show(); });
        HBox bottom = new HBox(btnLogout);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(5,0,0,0));
        root.setBottom(bottom);

        s.setScene(new Scene(crearFondo(root), 750, 600));
        s.show();
    }

    public static void main(String[] args) { launch(args); }
}