package avecAPI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import org.json.JSONObject;
import org.json.XML;
import java.io.*;
import java.nio.file.Files;

public class XMLJSONConverterAPI extends Application {
    private TextArea inputArea, outputArea;
    private Label statusLabel;
    private ComboBox<String> conversionMode;

    @Override
    public void start(Stage stage){
        BorderPane root=new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right,#667eea,#764ba2);");

        VBox header=new VBox(10); header.setAlignment(Pos.CENTER); header.setPadding(new Insets(20));
        Label title=new Label(" XML ⇄ JSON Converter ⚡"); title.setStyle("-fx-font-size:36px;-fx-text-fill:white;-fx-font-weight:bold;");
        conversionMode=new ComboBox<>(); conversionMode.getItems().addAll("XML → JSON","JSON → XML"); conversionMode.setValue("XML → JSON");
        header.getChildren().addAll(title,conversionMode);
        root.setTop(header);

        HBox center=new HBox(10); center.setPadding(new Insets(10));
        inputArea=new TextArea(); inputArea.setPromptText("Collez votre XML ou JSON ici..."); HBox.setHgrow(inputArea,Priority.ALWAYS);
        outputArea=new TextArea(); outputArea.setEditable(false); HBox.setHgrow(outputArea,Priority.ALWAYS);
        center.getChildren().addAll(inputArea,outputArea); root.setCenter(center);

        HBox buttons=new HBox(10); buttons.setAlignment(Pos.CENTER); buttons.setPadding(new Insets(10));
        Button convert=new Button("Convertir"); Button clear=new Button("Effacer"); Button load=new Button("Charger"); Button save=new Button("Sauvegarder");
        buttons.getChildren().addAll(convert,clear,load,save); root.setBottom(buttons);

        statusLabel=new Label("✨ Prêt"); root.setBottom(new VBox(buttons,statusLabel));

        convert.setOnAction(e->convert());
        clear.setOnAction(e->{inputArea.clear(); outputArea.clear(); statusLabel.setText("🧹 Effacé");});
        load.setOnAction(e->loadFile());
        save.setOnAction(e->saveFile());

        Scene scene=new Scene(root,1000,600); stage.setScene(scene); stage.show();
        FadeTransition f=new FadeTransition(Duration.millis(800),root); f.setFromValue(0); f.setToValue(1); f.play();
    }

    private void convert(){
        try{
            String in=inputArea.getText().trim();
            if(in.isEmpty()){statusLabel.setText("⚠️ Entrez du contenu"); return;}
            String out;
            if(conversionMode.getValue().equals("XML → JSON")){
                JSONObject json=XML.toJSONObject(in); out=json.toString(4);
            } else {
                JSONObject json=new JSONObject(in); out=XML.toString(json,4);
            }
            outputArea.setText(out); statusLabel.setText("✅ Conversion réussie");
        } catch(Exception e){outputArea.setText("❌ Erreur: "+e.getMessage()); statusLabel.setText("❌ Conversion échouée");}
    }

    private void loadFile(){
        FileChooser fc=new FileChooser(); fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML/JSON","*.xml","*.json"), new FileChooser.ExtensionFilter("Tous","*.*"));
        File f=fc.showOpenDialog(inputArea.getScene().getWindow()); if(f!=null) try{inputArea.setText(new String(Files.readAllBytes(f.toPath()))); statusLabel.setText("📂 Chargé: "+f.getName());}catch(IOException e){statusLabel.setText("❌ Erreur");}
    }

    private void saveFile(){
        FileChooser fc=new FileChooser(); fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON","*.json"), new FileChooser.ExtensionFilter("XML","*.xml"));
        File f=fc.showSaveDialog(outputArea.getScene().getWindow()); if(f!=null) try(FileWriter w=new FileWriter(f)){w.write(outputArea.getText()); statusLabel.setText("💾 Sauvegardé: "+f.getName());}catch(IOException e){statusLabel.setText("❌ Erreur");}
    }

    public static void main(String[] args){launch(args);}
}
