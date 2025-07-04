module com.example.wolfenstain {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires static lombok;

    opens com.example.wolfenstain to javafx.fxml;
    exports com.example.wolfenstain;
}