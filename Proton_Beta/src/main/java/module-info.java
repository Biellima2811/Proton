module com.mycompany.proton {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.mycompany.proton to javafx.fxml;
    exports com.mycompany.proton;
}
