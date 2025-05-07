package com.rplbo.datamhsfx.datamahasiswa;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class DataMhsController implements Initializable {
    private String imageUrl;
    private String nimlama;
    private Connection conn;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private Button btnCari;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnFilter;

    @FXML
    private Button btnHapus;

    @FXML
    private Button btnTambah;

    @FXML
    private Button btnTutup;

    @FXML
    private TableColumn<Mahasiswa, String> colNIM;

    @FXML
    private TableColumn<Mahasiswa, String> colNama;

    @FXML
    private ImageView imgFoto;

    @FXML
    private TableView<Mahasiswa> tblMhs;

    @FXML
    private TextField txtNIM;

    @FXML
    private TextField txtNama;

    @FXML
    private TextField txtNamaCari;

    @FXML
    private TextField txtNilai;

    @FXML
    private TextField txtNilaiFilter;

    protected void koneksiDB() throws SQLException,
            ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        //SQL Database connection params
        String connectionString =
                "jdbc:sqlite:D:/71230986/RPL-BO Week11/Guided/PRPLBO-2025-IvanRoberto83/src/mhs.sqlite";
        conn = DriverManager.getConnection(connectionString);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tblMhs.setEditable(false);
        colNIM.setCellValueFactory(new PropertyValueFactory<Mahasiswa,String>("nim"));
        colNama.setCellValueFactory(new PropertyValueFactory<Mahasiswa,String>("nama"));
        showMahasiswaDetails(null);
        try {
            koneksiDB();
            tblMhs.setItems(getDataFromTable("select * from mahasiswa"));
            tblMhs.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> showMahasiswaDetails((Mahasiswa) newValue));
            generateChart();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList dataBaseArrayList(ResultSet rs)
            throws SQLException {
        ArrayList<Mahasiswa> data =  new ArrayList<>();
        while (rs.next()) {
            Mahasiswa p = new
                    Mahasiswa(rs.getString("nim"),rs.getString("nama"),rs.getDouble("nilai"),rs.getString("foto"));
            data.add(p);
        }
        return data;
    }

    private void generateChart() throws SQLException {
        ObservableList<XYChart.Data<String, Number>> data = getNilaiFromTable("select nim,nilai from mahasiswa");
                XYChart.Series<String, Number> series = new XYChart.Series<>("Nilai", data);
        barChart.getData().add(series);
        barChart.getXAxis().setLabel("NIM");
        barChart.getYAxis().setLabel("Nilai");
    }

    private ObservableList<XYChart.Data<String, Number>> getNilaiFromTable(String sql) throws SQLException {
        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery();
        try{
            while(rs.next()){
                String nim= rs.getString("nim");
                double nilai = rs.getDouble("nilai");
                data.add(new XYChart.Data<>(nim, nilai));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return data;
    }

    private ObservableList<Mahasiswa> getDataFromTable(String query) throws SQLException {
        ResultSet rs;
        String select = query;
        PreparedStatement preparedStatement = conn.prepareStatement(select);
        rs = preparedStatement.executeQuery();
        ObservableList<Mahasiswa> mahasiswas = FXCollections.observableArrayList(dataBaseArrayList(rs));
        return mahasiswas;
    }

    private void showMahasiswaDetails(Mahasiswa mhs) {
        if (mhs != null) {
            txtNIM.setText(mhs.getNim());
            txtNama.setText(mhs.getNama());
            txtNilai.setText(String.valueOf(mhs.getNilai()));
            if(!mhs.getFoto().equals("")) {
                imgFoto.setImage(new Image(mhs.getFoto()));
                imageUrl = mhs.getFoto();
            }else {
                imageUrl = "";
                imgFoto.setImage(null);
            }
        } else {
            txtNIM.setText("");
            txtNama.setText("");
            txtNilai.setText("0");
            imgFoto.setImage(null);
        }
    }

    @FXML
    private void addData() throws SQLException, ClassNotFoundException {
        if (btnTambah.getText().equals("Tambah")) {
            clearFields();
            enableFields();
            btnTambah.setText("Simpan");
        } else {
            saveData();
            disableFields();
            btnTambah.setText("Tambah");
        }
    }

    private void clearFields() {
        txtNIM.clear();
        txtNama.clear();
        txtNilai.clear();
        imgFoto.setImage(null);
    }

    private void enableFields() {
        txtNIM.setEditable(true);
        txtNama.setEditable(true);
        txtNilai.setEditable(true);
        imgFoto.setDisable(false);
    }

    private void disableFields() {
        txtNIM.setEditable(false);
        txtNama.setEditable(false);
        txtNilai.setEditable(false);
        imgFoto.setDisable(true);
        imageUrl = "";
    }

    private void saveData() throws SQLException {
        String nim = txtNIM.getText();
        String nama = txtNama.getText();
        double nilai = Double.parseDouble(txtNilai.getText());
        String foto = imageUrl.isEmpty() ? "" : imageUrl;

        String query = "INSERT INTO mahasiswa VALUES(?,?,?,?)";
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, nim);
        preparedStatement.setString(2, nama);
        preparedStatement.setDouble(3, nilai);
        preparedStatement.setString(4, foto);
        preparedStatement.executeUpdate();

        tblMhs.setItems(getDataFromTable("SELECT * FROM mahasiswa"));
    }

    @FXML
    void ambilFoto(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.png", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                imageUrl = file.toURI().toURL().toExternalForm();
                Image image = new Image(imageUrl);
                imgFoto.setImage(image);
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @FXML
    protected void cariData() throws SQLException {
        if(btnCari.getText().equals("Cari")){
            if(!txtNamaCari.getText().isEmpty()){
                tblMhs.setItems(getDataFromTable("select * from mahasiswa where nama like '%"+txtNamaCari.getText()+"%'"));
                tblMhs.getSelectionModel().selectedItemProperty().addListener(
                        (observable, oldValue, newValue) -> showMahasiswaDetails((Mahasiswa) newValue));
            } else {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText("Hasil Pencarian");
                a.setContentText("Data Tidak Ditemukan!");
                a.showAndWait();
                txtNamaCari.clear();
            }
            btnCari.setText("Reset");
        } else {
            tblMhs.setItems(getDataFromTable("select * from mahasiswa"));
            btnCari.setText("Cari");
            txtNamaCari.clear();
        }
    }

    @FXML
    void deleteData(ActionEvent event) throws SQLException {
        if(txtNIM.getText().equals("")) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText("Hapus Data");
        a.setContentText("Apakah Anda yakin akan menghapus data ini?");
        Optional<ButtonType> jwb =  a.showAndWait();
        if(jwb.get() == ButtonType.OK){
            PreparedStatement preparedStatement = conn.prepareStatement("delete from mahasiswa where nim=?");
            preparedStatement.setString(1, txtNIM.getText());
            preparedStatement.executeUpdate();
            tblMhs.setItems(getDataFromTable("select * from mahasiswa"));
        }
    }

    @FXML
    protected void editData() throws SQLException {
        String foto;
        if(txtNIM.getText().equals("")) return;
        if(btnEdit.getText().equals("Edit")){
            nimlama = txtNIM.getText();
            txtNIM.setEditable(true);
            txtNama.setEditable(true);
            txtNilai.setEditable(true);
            btnEdit.setText("Simpan");
        } else {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setHeaderText("Edit Data");
            a.setContentText("Apakah Anda yakin akan mengganti data ini?");
            Optional<ButtonType> jwb = a.showAndWait();
            if(jwb.get() == ButtonType.OK){
                PreparedStatement preparedStatement = conn.prepareStatement("update mahasiswa set nim=?, nama=?, nilai=?, foto=? where nim=?");
                preparedStatement.setString(1, txtNIM.getText());
                preparedStatement.setString(2, txtNama.getText());
                preparedStatement.setDouble(3, Double.parseDouble(txtNilai.getText()));
                if(imageUrl.equals("")) foto = ""; else foto = imageUrl;
                preparedStatement.setString(4, foto);
                preparedStatement.setString(5, nimlama);
                int hasil = preparedStatement.executeUpdate();
                tblMhs.setItems(getDataFromTable("select * from mahasiswa"));
                imageUrl = "";
            }
            txtNIM.setEditable(false);
            txtNama.setEditable(false);
            txtNilai.setEditable(false);
            imageUrl = "";
            btnEdit.setText("Edit");
        }
    }

    @FXML
    protected void filterNilai() throws SQLException {
        if(btnFilter.getText().equals("Filter Nilai >=")){
            if(!txtNilaiFilter.getText().isEmpty()){
                tblMhs.setItems(getDataFromTable("select * from mahasiswa where nilai>="+txtNilaiFilter.getText()));
                tblMhs.getSelectionModel().selectedItemProperty().addListener(
                        (observable, oldValue, newValue) -> showMahasiswaDetails((Mahasiswa) newValue));
            } else {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText("Hasil Filter");
                a.setContentText("Data Tidak Ditemukan!");
                a.showAndWait();
                txtNilaiFilter.clear();
            }
            btnFilter.setText("Reset");
        } else {
            tblMhs.setItems(getDataFromTable("select * from mahasiswa"));
            btnFilter.setText("Filter Nilai >=");
            txtNilaiFilter.clear();
        }
    }

    @FXML
    void onTutupClick(ActionEvent event) {
        Platform.exit();
    }

}