package sansAPI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class XMLJSONConverterNoAPI extends Application {
    private TextArea inputArea, outputArea;
    private Label statusLabel;
    private ComboBox<String> conversionMode;
    private static final String INDENT="  ";

    @Override
    public void start(Stage stage){
        BorderPane root=new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");

        VBox header=new VBox(15); header.setPadding(new Insets(30,30,20,30)); header.setAlignment(Pos.CENTER);
        Label title=new Label(" XML ⇄ JSON Converter ⚡"); title.setStyle("-fx-font-size:42px;-fx-font-weight:bold;-fx-text-fill:white;-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.3),10,0,0,3);");
        Label subtitle=new Label("Conversion native sans dépendances externes"); subtitle.setStyle("-fx-font-size:16px;-fx-text-fill:rgba(255,255,255,0.9);");
        HBox modeBox=new HBox(15); modeBox.setAlignment(Pos.CENTER);
        Label modeLabel=new Label("Mode:"); modeLabel.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;");
        conversionMode=new ComboBox<>(); conversionMode.getItems().addAll("XML → JSON","JSON → XML"); conversionMode.setValue("XML → JSON");
        conversionMode.setStyle("-fx-background-color:white;-fx-font-size:14px;-fx-background-radius:20;-fx-padding:8 20;");
        modeBox.getChildren().addAll(modeLabel,conversionMode);
        header.getChildren().addAll(title,subtitle,modeBox);
        root.setTop(header);

        HBox content=new HBox(20); content.setPadding(new Insets(20,30,20,30)); content.setAlignment(Pos.CENTER);
        VBox inBox=createBox("📥 Entrée",true); inputArea=(TextArea)((VBox)inBox.getChildren().get(1)).getChildren().get(0);
        VBox outBox=createBox("📤 Sortie",false); outputArea=(TextArea)((VBox)outBox.getChildren().get(1)).getChildren().get(0);
        HBox.setHgrow(inBox, Priority.ALWAYS); HBox.setHgrow(outBox, Priority.ALWAYS);
        content.getChildren().addAll(inBox,outBox); root.setCenter(content);

        VBox bottom=new VBox(15); bottom.setPadding(new Insets(20,30,30,30)); bottom.setAlignment(Pos.CENTER);
        HBox btnBox=new HBox(15); btnBox.setAlignment(Pos.CENTER);
        Button convertBtn=createBtn("🚀 Convertir","#4CAF50"), clearBtn=createBtn("🗑️ Effacer","#f44336"),
                loadBtn=createBtn("📂 Charger","#2196F3"), saveBtn=createBtn("💾 Sauvegarder","#FF9800"),
                copyBtn=createBtn("📋 Copier","#9C27B0");
        convertBtn.disableProperty().bind(inputArea.textProperty().isEmpty());
        convertBtn.setOnAction(e->convert()); clearBtn.setOnAction(e->clearAll()); loadBtn.setOnAction(e->loadFile());
        saveBtn.setOnAction(e->saveFile()); copyBtn.setOnAction(e->{ClipboardContent c=new ClipboardContent(); c.putString(outputArea.getText()); Clipboard.getSystemClipboard().setContent(c);});
        btnBox.getChildren().addAll(convertBtn,clearBtn,loadBtn,saveBtn,copyBtn);
        statusLabel=new Label("✨ Prêt à convertir"); statusLabel.setStyle("-fx-font-size:14px;-fx-text-fill:white;-fx-background-color:rgba(0,0,0,0.3);-fx-padding:10 20;-fx-background-radius:20;");
        bottom.getChildren().addAll(btnBox,statusLabel); root.setBottom(bottom);

        Scene scene=new Scene(root,1400,800); stage.setScene(scene); stage.show();
        FadeTransition f=new FadeTransition(Duration.millis(800),root); f.setFromValue(0); f.setToValue(1); f.play();
    }

    private VBox createBox(String t,boolean input){
        VBox b=new VBox(10); b.setStyle("-fx-background-color: rgba(255,255,255,0.95);-fx-background-radius:20;-fx-padding:20;-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.2),15,0,0,5);");
        Label l=new Label(t); l.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#667eea;");
        TextArea a=new TextArea(); a.setPromptText(input?"Collez votre XML ou JSON ici...":"Résultat de la conversion..."); a.setWrapText(true);
        a.setStyle("-fx-font-family:'Consolas','Monaco',monospace;-fx-font-size:13px;-fx-background-color:#f8f9fa;-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#e0e0e0;-fx-border-width:1;");
        if(!input) a.setEditable(false); VBox.setVgrow(a,Priority.ALWAYS); VBox vc=new VBox(a); VBox.setVgrow(vc,Priority.ALWAYS);
        b.getChildren().addAll(l,vc); VBox.setVgrow(b,Priority.ALWAYS); return b;
    }

    private Button createBtn(String t,String c){ Button b=new Button(t); b.setStyle("-fx-background-color:"+c+";-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;-fx-padding:12 30;-fx-background-radius:25;-fx-cursor:hand;-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.3),8,0,0,3);"); b.setOnMouseEntered(e->{b.setScaleX(1.05); b.setScaleY(1.05);}); b.setOnMouseExited(e->{b.setScaleX(1); b.setScaleY(1);}); return b;}

    private void convert(){
        String in=inputArea.getText().trim(); if(in.isEmpty()){showStatus("⚠️ Veuillez entrer du contenu","#FF9800");return;}
        try{String r=conversionMode.getValue().equals("XML → JSON")?xmlToJson(in):jsonToXml(in); outputArea.setText(r); showStatus("✅ Conversion réussie!","#4CAF50");}catch(Exception e){outputArea.setText("❌ Erreur: "+e.getMessage()); showStatus("❌ Erreur de conversion","#f44336");}
    }

    private String xmlToJson(String x)throws Exception{DocumentBuilderFactory f=DocumentBuilderFactory.newInstance(); f.setIgnoringElementContentWhitespace(true); f.setNamespaceAware(true); DocumentBuilder b=f.newDocumentBuilder(); Document d=b.parse(new InputSource(new StringReader(x))); StringBuilder s=new StringBuilder("{\n"); elementToJson(d.getDocumentElement(),s,1); s.append("\n}"); return s.toString();}
    private void elementToJson(Element e,StringBuilder s,int i){String ind=INDENT.repeat(i); s.append(ind).append("\"").append(e.getNodeName()).append("\": "); NodeList c=e.getChildNodes(); List<Element> ce=new ArrayList<>(); StringBuilder t=new StringBuilder(); for(int j=0;j<c.getLength();j++){Node n=c.item(j); if(n.getNodeType()==Node.ELEMENT_NODE) ce.add((Element)n); else if(n.getNodeType()==Node.TEXT_NODE){String txt=n.getTextContent().trim(); if(!txt.isEmpty()) t.append(txt);}} NamedNodeMap a=e.getAttributes(); if(ce.isEmpty() && a.getLength()==0){s.append(t.length()>0?"\""+escapeJson(t.toString())+"\"":"\"\"");} else{s.append("{\n"); if(a.getLength()>0){for(int j=0;j<a.getLength();j++){Node at=a.item(j); s.append(ind).append(INDENT).append("\"@").append(at.getNodeName()).append("\":\"").append(escapeJson(at.getNodeValue())).append("\""); if(j<a.getLength()-1||ce.size()>0||t.length()>0) s.append(","); s.append("\n");}} if(t.length()>0 && ce.isEmpty()) s.append(ind).append(INDENT).append("\"#text\":\"").append(escapeJson(t.toString())).append("\"\n"); else if(!ce.isEmpty()){Map<String,List<Element>> g=new HashMap<>(); for(Element ch:ce) g.computeIfAbsent(ch.getNodeName(),k->new ArrayList<>()).add(ch); int count=0; for(Map.Entry<String,List<Element>>en:g.entrySet()){List<Element> ls=en.getValue(); if(ls.size()==1) elementToJson(ls.get(0),s,i+1); else{s.append(ind).append(INDENT).append("\"").append(en.getKey()).append("\":[\n"); for(int k=0;k<ls.size();k++){Element ch=ls.get(k); s.append(ind).append(INDENT).append(INDENT).append("{\n"); elementToJson(ch,s,i+2); s.append(ind).append(INDENT).append(INDENT).append("}"); if(k<ls.size()-1) s.append(","); s.append("\n");} s.append(ind).append(INDENT).append("]");} if(++count<g.size()) s.append(","); s.append("\n");}} s.append(ind).append("}");}}

    private String jsonToXml(String j)throws Exception{j=j.trim(); StringBuilder s=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); parseJsonToXml(j,s,0); return s.toString();}
    private void parseJsonToXml(String j,StringBuilder s,int d){j=j.trim(); String ind=INDENT.repeat(d); if(j.startsWith("{")){j=j.substring(1,j.length()-1).trim(); String[] p=splitJson(j); if(p.length>0){String k=p[0].trim(); int c=k.indexOf(':'); if(c>0){String key=k.substring(0,c).trim().replaceAll("^\"|\"$","").replaceAll("^@",""); s.append(ind).append("<").append(key).append(">\n"); for(String pair:p) processPair(pair,s,d+1); s.append(ind).append("</").append(key).append(">\n");}}}}
    private void processPair(String p,StringBuilder s,int d){p=p.trim(); int c=p.indexOf(':'); if(c<0) return; String k=p.substring(0,c).trim().replaceAll("^\"|\"$",""); String v=p.substring(c+1).trim(); String ind=INDENT.repeat(d); if(k.equals("#text")) s.append(ind).append(v.replaceAll("^\"|\"$","")).append("\n"); else if(k.startsWith("@")){} else{if(v.startsWith("{")){s.append(ind).append("<").append(k).append(">\n"); parseNested(v,s,d+1); s.append(ind).append("</").append(k).append(">\n");} else if(v.startsWith("[")) parseArray(k,v,s,d); else{String val=v.replaceAll("^\"|\"$","").replaceAll(",$",""); s.append(ind).append("<").append(k).append(">").append(val).append("</").append(k).append(">\n");}}}
    private void parseNested(String j,StringBuilder s,int d){j=j.substring(1,j.length()-1).trim(); for(String p:splitJson(j)) processPair(p,s,d);}
    private void parseArray(String k,String a,StringBuilder s,int d){a=a.substring(1,a.length()-1).trim(); String[] items=splitJson(a); String ind=INDENT.repeat(d); for(String i:items){s.append(ind).append("<").append(k).append(">"); s.append(i.trim().replaceAll("^\"|\"$","").replaceAll(",$","")); s.append("</").append(k).append(">\n");}}
    private String[] splitJson(String j){List<String> l=new ArrayList<>(); StringBuilder c=new StringBuilder(); int bd=0,brd=0; boolean str=false; for(char ch:j.toCharArray()){if(ch=='"' && (c.length()==0||c.charAt(c.length()-1)!='\\')) str=!str; if(!str){if(ch=='{') bd++; else if(ch=='}') bd--; else if(ch=='[') brd++; else if(ch==']') brd--;} if(ch==',' && bd==0 && brd==0 && !str){l.add(c.toString()); c=new StringBuilder();} else c.append(ch);} if(c.length()>0) l.add(c.toString()); return l.toArray(new String[0]);}
    private String escapeJson(String t){return t.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");}

    private void clearAll(){inputArea.clear(); outputArea.clear(); showStatus("🧹 Zones effacées","#2196F3");}
    private void loadFile(){FileChooser f=new FileChooser(); f.setTitle("Charger un fichier"); f.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichiers XML/JSON","*.xml","*.json"),new FileChooser.ExtensionFilter("Tous les fichiers","*.*")); File file=f.showOpenDialog(inputArea.getScene().getWindow()); if(file!=null){try{inputArea.setText(new String(Files.readAllBytes(file.toPath()))); showStatus("📂 Fichier chargé: "+file.getName(),"#4CAF50");}catch(IOException e){showStatus("❌ Erreur de chargement","#f44336");}}}
    private void saveFile(){FileChooser f=new FileChooser(); f.setTitle("Sauvegarder le résultat"); f.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichiers JSON","*.json"),new FileChooser.ExtensionFilter("Fichiers XML","*.xml")); File file=f.showSaveDialog(outputArea.getScene().getWindow()); if(file!=null){try(FileWriter w=new FileWriter(file)){w.write(outputArea.getText()); showStatus("💾 Fichier sauvegardé: "+file.getName(),"#4CAF50");}catch(IOException e){showStatus("❌ Erreur de sauvegarde","#f44336");}}}
    private void showStatus(String m,String c){statusLabel.setText(m); statusLabel.setStyle(statusLabel.getStyle().replaceAll("-fx-background-color:[^;]+","-fx-background-color:"+c));}

    public static void main(String[] args){launch(args);}
}
