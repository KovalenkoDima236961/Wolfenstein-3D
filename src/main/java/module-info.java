module com.example.wolfenstein {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires static lombok;

    opens com.example.wolfenstein to javafx.fxml;
    exports com.example.wolfenstein;
}