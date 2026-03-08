package application;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class dashboardController implements Initializable {

    private static final String API_BASE_URL = "http://127.0.0.1:5000/api";
    private static final String API_PREDICT_URL = API_BASE_URL + "/predict";
    private static final Duration API_TIMEOUT = Duration.ofSeconds(20);
    private static final String IMAGE_STORAGE_DIR = "data/images";
    private static final boolean USE_DATABASE = false;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button close;

    @FXML
    private Button minimize;

    @FXML
    private Label username;

    @FXML
    private Button home_btn;

    @FXML
    private Button addPatient_btn;

    @FXML
    private Button logout_btn;

    @FXML
    private AnchorPane home_form;

    @FXML
    private Label home_totalPatients;

    @FXML
    private Label home_totalAdmitted;

    @FXML
    private BarChart home_totalDischarged;

    @FXML
    private BarChart<?, ?> home_chart;

    @FXML
    private AnchorPane addPatient_form;

    @FXML
    private TableView<patientData> addPatient_tableView;

    @FXML
    private TableColumn<patientData, String> addPatient_col_status;

    @FXML
    private TableColumn<patientData, String> addPatient_col_patientID;

    @FXML
    private TableColumn<patientData, String> addPatient_col_name;

    @FXML
    private TableColumn<patientData, Integer> addPatient_col_age;

    @FXML
    private TableColumn<patientData, String> addPatient_col_gender;

    @FXML
    private TableColumn<patientData, String> addPatient_col_phoneNumber;

    @FXML
    private TableColumn<patientData, String> addPatient_col_bloodGroup;

    @FXML
    private TableColumn<patientData, LocalDate> addPatient_col_date;

    @FXML
    private TextField addPatient_search;

    @FXML
    private TextField addPatient_patientID;

    @FXML
    private TextField addPatient_name;

    @FXML
    private ComboBox<String> addPatient_status;

    @FXML
    private TextField addPatient_age;

    @FXML
    private ComboBox<String> addPatient_gender;

    @FXML
    private TextField addPatient_phoneNumber;

    @FXML
    private ComboBox<String> addPatient_bloodGroup;

    @FXML
    private ImageView addPatient_image;

    @FXML
    private Button addPatient_importBtn;

    @FXML
    private Button addPatient_addBtn;

    @FXML
    private Button addPatient_updateBtn;

    @FXML
    private Button addPatient_deleteBtn;

    @FXML
    private Button addPatient_clearBtn;

    @FXML
    private Label home_totalInactivePa;

    private Image image;
    private String lastPrediction;
    private String lastConfidence;
    private boolean hasPrognosisColumn;
    private final ObservableList<patientData> testPatientList = FXCollections.observableArrayList();

    @FXML
    private Button salary_btn;

    @FXML
    private AnchorPane salary_form;

    @FXML
    private TableView<patientData> patientDetails_tableView;

    @FXML
    private TableColumn<patientData, Integer> patientDetails_col_patientID;

    @FXML
    private TableColumn<patientData, String> patientDetails_col_name;

    @FXML
    private TableColumn<patientData, Integer> patientDetails_col_age;

    @FXML
    private TableColumn<patientData, String> patientDetails_col_bloodGroup;

    @FXML
    private TableColumn<patientData, String> patientDetails_col_prognosis;

    @FXML
    private TextField patientDetails_patientID;

    @FXML
    private Label patientDetails_name;

    @FXML
    private Label patientDetails_age;

    @FXML
    private Label patientDetails_bloodGroup;

    @FXML
    private TextField patientDetails_prognosis;

   public void homePatientTotalPatients() {
    String sql = "SELECT COUNT(patientID) AS total FROM patient";

    int countData = 0;

    try (Connection connect = database.connectDb()) {
        assert connect != null;
        try (Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (result.next()) {
                countData = result.getInt("total");
            }

            home_totalPatients.setText(String.valueOf(countData));

        }
    } catch (Exception e) {
        e.printStackTrace();
        }
    }

    public void homeTotalInactive() {
        String sql = "SELECT COUNT(patientID) AS total FROM patient WHERE status = 'inactive'";

        int countData = 0;

        try (Connection connect = database.connectDb();
             PreparedStatement prepare = connect.prepareStatement(sql);
             ResultSet result = prepare.executeQuery()) {

            if (result.next()) {
                countData = result.getInt("total");
            }

            home_totalInactivePa.setText(String.valueOf(countData));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void homeChart() {

        home_chart.getData().clear();

        String sql = "SELECT admissionDate, COUNT(patientID) " +
                "FROM patient " +
                "GROUP BY admissionDate " +
                "ORDER BY admissionDate ASC LIMIT 7";

        connect = database.connectDb();

        try {
            XYChart.Series chart = new XYChart.Series();

            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            while (result.next()) {
                chart.getData().add(new XYChart.Data(result.getString(1), result.getInt(2)));
            }

            home_chart.getData().add(chart);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void addPatientAdd() {

        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        // String checkSql = "SELECT patientID FROM patient WHERE patientID = ?";
        // String insertSql = "INSERT INTO patient "
        //         + "(patientID, name, age, gender, phoneNumber, bloodGroup, image, admissionDate, status) "
        //         + "VALUES(?,?,?,?,?,?,?,?,?)";
        // String insertSqlWithPrognosis = "INSERT INTO patient "
        //         + "(patientID, name, age, gender, phoneNumber, bloodGroup, image, admissionDate, status, prognosis) "
        //         + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        //
        // connect = database.connectDb();

        try {

            if (addPatient_patientID.getText().isEmpty()
                    || addPatient_name.getText().isEmpty()
                    || addPatient_age.getText().isEmpty()
                    || addPatient_gender.getSelectionModel().getSelectedItem() == null
                    || addPatient_phoneNumber.getText().isEmpty()
                    || addPatient_bloodGroup.getSelectionModel().getSelectedItem() == null
                    || addPatient_status.getSelectionModel().getSelectedItem() == null
                    || getData.path == null || getData.path.equals("")) {

                showAlert(Alert.AlertType.ERROR, "Please fill all blank fields");
                return;
            }

            int patientId = Integer.parseInt(addPatient_patientID.getText());
            int age = Integer.parseInt(addPatient_age.getText());

            String prognosisValue = buildPrognosisValue();
            if (!USE_DATABASE) {
                int existingIndex = findTestPatientIndex(patientId);
                if (existingIndex >= 0) {
                    showAlert(Alert.AlertType.ERROR, "Patient ID already exists!");
                    return;
                }

                patientData patient = new patientData(
                        patientId,
                        addPatient_name.getText(),
                        age,
                        addPatient_gender.getValue().toString(),
                        addPatient_phoneNumber.getText(),
                        addPatient_bloodGroup.getValue().toString(),
                        getData.path,
                        date,
                        addPatient_status.getValue().toString(),
                        prognosisValue
                );
                testPatientList.add(patient);
            } else {
                // prepare = connect.prepareStatement(checkSql);
                // prepare.setInt(1, patientId);
                // result = prepare.executeQuery();
                //
                // if (result.next()) {
                //     showAlert(Alert.AlertType.ERROR, "Patient ID already exists!");
                //     return;
                // }
                //
                // if (hasPrognosisColumn) {
                //     prepare = connect.prepareStatement(insertSqlWithPrognosis);
                // } else {
                //     prepare = connect.prepareStatement(insertSql);
                // }
                // prepare.setInt(1, patientId);
                // prepare.setString(2, addPatient_name.getText());
                // prepare.setInt(3, age);
                // prepare.setString(4, addPatient_gender.getValue().toString());
                // prepare.setString(5, addPatient_phoneNumber.getText());
                // prepare.setString(6, addPatient_bloodGroup.getValue().toString());
                // prepare.setString(7, getData.path);
                // prepare.setString(9, addPatient_status.getValue().toString());
                // prepare.setDate(8, sqlDate);
                // if (hasPrognosisColumn) {
                //     prepare.setString(10, prognosisValue);
                // }
                //
                // prepare.executeUpdate();
            }

            showAlert(Alert.AlertType.INFORMATION, "Patient Added Successfully!");

            addPatientShowListData();
            patientDetailsShowListData();
            addPatientReset();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID and Age must be numbers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to add patient: " + e.getMessage());
        }
    }


    public void addPatientUpdate() {

        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        // String updateSql = "UPDATE patient SET "
        //         + "name = ?, "
        //         + "age = ?, "
        //         + "gender = ?, "
        //         + "phoneNumber = ?, "
        //         + "bloodGroup = ?, "
        //         + "image = ?, "
        //         + "admissionDate = ?, "
        //         + "status = ? "
        //         + "WHERE patientID = ?";
        // String updateSqlWithPrognosis = "UPDATE patient SET "
        //         + "name = ?, "
        //         + "age = ?, "
        //         + "gender = ?, "
        //         + "phoneNumber = ?, "
        //         + "bloodGroup = ?, "
        //         + "image = ?, "
        //         + "admissionDate = ?, "
        //         + "status = ?, "
        //         + "prognosis = ? "
        //         + "WHERE patientID = ?";
        //
        // connect = database.connectDb();

        try {

            if (addPatient_patientID.getText().isEmpty()
                    || addPatient_name.getText().isEmpty()
                    || addPatient_age.getText().isEmpty()
                    || addPatient_gender.getSelectionModel().getSelectedItem() == null
                    || addPatient_phoneNumber.getText().isEmpty()
                    || addPatient_bloodGroup.getSelectionModel().getSelectedItem() == null
                    || addPatient_status.getSelectionModel().getSelectedItem() == null
                    || getData.path == null || getData.path.equals(""))  {

                showAlert(Alert.AlertType.ERROR, "Please fill all blank fields");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to UPDATE Patient ID: "
                    + addPatient_patientID.getText() + "?");

            Optional<ButtonType> option = confirm.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {

                int patientId = Integer.parseInt(addPatient_patientID.getText());
                int age = Integer.parseInt(addPatient_age.getText());
                String prognosisValue = buildPrognosisValue();

                if (!USE_DATABASE) {
                    int existingIndex = findTestPatientIndex(patientId);
                    if (existingIndex < 0) {
                        showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                        return;
                    }

                    patientData patient = new patientData(
                            patientId,
                            addPatient_name.getText(),
                            age,
                            addPatient_gender.getValue().toString(),
                            addPatient_phoneNumber.getText(),
                            addPatient_bloodGroup.getValue().toString(),
                            getData.path,
                            date,
                            addPatient_status.getValue().toString(),
                            prognosisValue
                    );
                    testPatientList.set(existingIndex, patient);
                } else {
                    // if (hasPrognosisColumn) {
                    //     prepare = connect.prepareStatement(updateSqlWithPrognosis);
                    // } else {
                    //     prepare = connect.prepareStatement(updateSql);
                    // }
                    //
                    // prepare.setString(1, addPatient_name.getText());
                    // prepare.setInt(2, age);
                    // prepare.setString(3, addPatient_gender.getValue().toString());
                    // prepare.setString(4, addPatient_phoneNumber.getText());
                    // prepare.setString(5, addPatient_bloodGroup.getValue().toString());
                    // prepare.setString(6, getData.path);
                    // prepare.setDate(7, sqlDate);
                    // prepare.setString(8, addPatient_status.getValue().toString());
                    // if (hasPrognosisColumn) {
                    //     prepare.setString(9, prognosisValue);
                    //     prepare.setInt(10, patientId);
                    // } else {
                    //     prepare.setInt(9, patientId);
                    // }
                    //
                    // prepare.executeUpdate();
                }

                showAlert(Alert.AlertType.INFORMATION, "Patient Updated Successfully!");

                addPatientShowListData();
                patientDetailsShowListData();
                addPatientReset();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID and Age must be numbers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update patient: " + e.getMessage());
        }
    }
    public void addPatientDelete() {

        // String deleteSql = "DELETE FROM patient WHERE patientID = ?";
        //
        // connect = database.connectDb();

        try {

            if (addPatient_patientID.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please enter Patient ID to delete.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to DELETE Patient ID: "
                    + addPatient_patientID.getText() + "?");

            Optional<ButtonType> option = confirm.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {

                int patientId = Integer.parseInt(addPatient_patientID.getText());

                if (!USE_DATABASE) {
                    int existingIndex = findTestPatientIndex(patientId);
                    if (existingIndex < 0) {
                        showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                        return;
                    }
                    testPatientList.remove(existingIndex);
                } else {
                    // prepare = connect.prepareStatement(deleteSql);
                    // prepare.setInt(1, patientId);
                    // prepare.executeUpdate();
                }

                showAlert(Alert.AlertType.INFORMATION, "Patient Deleted Successfully!");

                addPatientShowListData();
                addPatientReset();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPatientReset() {

        addPatient_patientID.setText("");
        addPatient_name.setText("");
        addPatient_age.setText("");
        addPatient_gender.getSelectionModel().clearSelection();
        addPatient_bloodGroup.getSelectionModel().clearSelection();
        addPatient_phoneNumber.setText("");
        addPatient_image.setImage(null);
        addPatient_status.getSelectionModel().clearSelection();

        getData.path = "";
        lastPrediction = null;
        lastConfidence = null;
    }

    public void addPatientInsertImage() {

        FileChooser open = new FileChooser();

        open.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = open.showOpenDialog(main_form.getScene().getWindow());

        if (file != null) {
            try {
                getData.path = storeImage(file);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to store image: " + e.getMessage());
                return;
            }

            Image image = new Image(file.toURI().toString(), 101, 127, false, true);
            addPatient_image.setImage(image);
            requestPredictionAsync(file);
        }
    }

    private void requestPredictionAsync(File file) {
        Thread worker = new Thread(() -> {
            try {
                String response = requestPrediction(file);
                String prediction = extractJsonValue(response, "prediction");
                String confidence = extractJsonValue(response, "confidence");
                lastPrediction = prediction;
                lastConfidence = confidence;
                String message = formatPredictionMessage(response);
                Platform.runLater(() -> {
                    if (prediction != null && patientDetails_prognosis != null) {
                        patientDetails_prognosis.setText(buildPrognosisValue());
                    }
                    showAlert(Alert.AlertType.INFORMATION, message);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "Prediction request failed: " + e.getMessage()));
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private String requestPrediction(File file) throws Exception {
        String boundary = "----JavaFXBoundary" + UUID.randomUUID();
        byte[] body = buildMultipartBody(file, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_PREDICT_URL))
                .timeout(API_TIMEOUT)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(API_TIMEOUT)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return "Prediction failed: HTTP " + response.statusCode() + " - " + response.body();
        }

        return response.body();
    }

    private byte[] buildMultipartBody(File file, String boundary) throws Exception {
        String lineBreak = "\r\n";
        StringBuilder header = new StringBuilder();
        header.append("--").append(boundary).append(lineBreak);
        header.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(file.getName()).append("\"").append(lineBreak);
        header.append("Content-Type: ").append(guessContentType(file.getName())).append(lineBreak);
        header.append(lineBreak);

        byte[] headerBytes = header.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] footerBytes = (lineBreak + "--" + boundary + "--" + lineBreak)
                .getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);
        return body;
    }

    private String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private String formatPredictionMessage(String json) {
        String prediction = extractJsonValue(json, "prediction");
        String confidence = extractJsonValue(json, "confidence");
        if (prediction == null) {
            return "Prediction response: " + json;
        }
        if (confidence == null) {
            return "Prediction: " + prediction;
        }
        return "Prediction: " + prediction + " (confidence " + confidence + ")";
    }

    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String buildPrognosisValue() {
        if (lastPrediction == null || lastPrediction.isEmpty()) {
            return "Pending";
        }
        if (lastConfidence == null || lastConfidence.isEmpty()) {
            return lastPrediction;
        }
        return lastPrediction + " (" + lastConfidence + ")";
    }

    private final String[] b_groupList = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    public void addPatientBloodGroupList() {
        ObservableList<String> listData = FXCollections.observableArrayList(b_groupList);
        addPatient_bloodGroup.setItems(listData);
    }

    private String[] listGender = {"Male", "Female"};

    public void addPatientGenderList() {
        List<String> listG = new ArrayList<>();

        for (String data : listGender) {
            listG.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listG);
        addPatient_gender.setItems(listData);
    }

    public void addPatientStatusList() {

        if (addPatient_status == null) {
            System.out.println("Status ComboBox not injected!");
            return;
        }

        if (!addPatient_status.getItems().isEmpty()) {
            return;
        }

        ObservableList<String> listData = FXCollections.observableArrayList(
                "Active",
                "Inactive"
        );

        addPatient_status.setItems(listData);

    }

    public void addPatientSearch() {
        FilteredList<patientData> filter = new FilteredList<>(addPatientList, e -> true);

        addPatient_search.textProperty().addListener((observable, oldValue, newValue) -> {

            filter.setPredicate(predicatePatientData -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchKey = newValue.toLowerCase();

                if (predicatePatientData.getPatientId().toString().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getName().toLowerCase().contains(searchKey)) {
                    return true;
                }else if (predicatePatientData.getStatus().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (Integer.toString(predicatePatientData.getAge()).contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getGender().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getPhoneNum().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getBloodGroup().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getAdmissionDate().toString().contains(searchKey)) {
                    return true;
                } else {
                    return false;
                }
            });
        });

        SortedList<patientData> sortList = new SortedList<>(filter);
        sortList.comparatorProperty().bind(addPatient_tableView.comparatorProperty());
        addPatient_tableView.setItems(sortList);
    }
    private Connection connect;
    private Statement statement;
    private PreparedStatement prepare;
    private ResultSet result;
    public ObservableList<patientData> addPatientListData() {

        if (!USE_DATABASE) {
            return testPatientList;
        }

        // ObservableList<patientData> listData = FXCollections.observableArrayList();
        // String sql = "SELECT * FROM patient";
        //
        // connect = database.connectDb();
        //
        // try {
        //     prepare = connect.prepareStatement(sql);
        //     result = prepare.executeQuery();
        //
        //     while (result.next()) {
        //
        //         patientData patient = new patientData(
        //                 result.getInt("patientID"),
        //                 result.getString("name"),
        //                 result.getInt("age"),
        //                 result.getString("gender"),
        //                 result.getString("phoneNumber"),
        //                 result.getString("bloodGroup"),
        //                 result.getString("image"),
        //                 result.getDate("admissionDate").toLocalDate(),
        //                 result.getString("status"),
        //                 getOptionalString(result, "prognosis")
        //         );
        //
        //         listData.add(patient);
        //     }
        //
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        //
        // return listData;
        return FXCollections.observableArrayList();
    }
    private ObservableList<patientData> addPatientList;

    public void addPatientShowListData() {
        addPatientList = addPatientListData();

        addPatient_col_patientID.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        addPatient_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        addPatient_col_age.setCellValueFactory(new PropertyValueFactory<>("age"));
        addPatient_col_gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        addPatient_col_phoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));
        addPatient_col_bloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        addPatient_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        addPatient_col_date.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));

        addPatient_tableView.setItems(addPatientList);
    }

    public void addPatientSelect() {
        patientData selectedPatient = addPatient_tableView.getSelectionModel().getSelectedItem();
        int selectedIndex = addPatient_tableView.getSelectionModel().getSelectedIndex();

        if (selectedPatient == null || selectedIndex < 0) {
            return;
        }

        addPatient_patientID.setText(String.valueOf(selectedPatient.getPatientId()));
        addPatient_name.setText(selectedPatient.getName());
        addPatient_status.setValue(String.valueOf(selectedPatient.getStatus()));
        addPatient_age.setText(String.valueOf(selectedPatient.getAge()));
        addPatient_phoneNumber.setText(selectedPatient.getPhoneNum());
        addPatient_gender.setValue(selectedPatient.getGender());
        addPatient_bloodGroup.setValue(selectedPatient.getBloodGroup());

        getData.path = selectedPatient.getImage();
        String resolvedPath = resolveImagePath(selectedPatient.getImage());
        if (resolvedPath == null) {
            addPatient_image.setImage(null);
        } else {
            String uri = "file:" + resolvedPath;
            Image image = new Image(uri, 101, 127, false, true);
            addPatient_image.setImage(image);
        }
    }

    /*public void salaryUpdate() {

        String sql = "UPDATE employee_info SET salary = '" + salary_salary.getText()
                + "' WHERE employee_id = '" + salary_employeeID.getText() + "'";

        connect = database.connectDb();

        try {
            Alert alert;

            if (salary_employeeID.getText().isEmpty()
                    || salary_firstName.getText().isEmpty()
                    || salary_lastName.getText().isEmpty()
                    || salary_position.getText().isEmpty()) {
                alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please select item first");
                alert.showAndWait();
            } else {
                statement = connect.createStatement();
                statement.executeUpdate(sql);

                alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Information Message");
                alert.setHeaderText(null);
                alert.setContentText("Successfully Updated!");
                alert.showAndWait();

                salaryShowListData();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void salaryReset() {
        salary_employeeID.setText("");
        salary_firstName.setText("");
        salary_lastName.setText("");
        salary_position.setText("");
        salary_salary.setText("");
    }

    public ObservableList<patientData> salaryListData() {

        ObservableList<patientData> listData = FXCollections.observableArrayList();

        String sql = "SELECT * FROM employee_info";

        connect = database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            patientData employeeD;

            while (result.next()) {
               employeeD = new patientData(result.getInt("employee_id"),
                         result.getString("firstName"),
                         result.getString("lastName"),
                         result.getString("position"),
                         result.getDouble("salary"));

                listData.add(employeeD);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return listData;
    }

    private ObservableList<patientData> salaryList;

    public void salaryShowListData() {
        salaryList = salaryListData();

        salary_col_employeeID.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        salary_col_firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        salary_col_lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        salary_col_position.setCellValueFactory(new PropertyValueFactory<>("position"));
        salary_col_salary.setCellValueFactory(new PropertyValueFactory<>("salary"));

        salary_tableView.setItems(salaryList);

    }

    public void salarySelect() {

        patientData employeeD = salary_tableView.getSelectionModel().getSelectedItem();
        int num = salary_tableView.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1) {
            return;
        }

        salary_employeeID.setText(String.valueOf(employeeD.getEmployeeId()));
        salary_firstName.setText(employeeD.getName());
        salary_lastName.setText(employeeD.getAge());
        salary_position.setText(employeeD.getBloodGroup());
        salary_salary.setText(String.valueOf(employeeD.getSalary()));

    }*/

    public void defaultNav() {
        home_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
    }

    public void switchForm(ActionEvent event) {

        if (event.getSource() == home_btn) {

            home_form.setVisible(true);
            addPatient_form.setVisible(false);
            salary_form.setVisible(false);

            home_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            addPatient_btn.setStyle("-fx-background-color:transparent");
            salary_btn.setStyle("-fx-background-color:transparent");

            homePatientTotalPatients();
            homeTotalInactive();
            homeChart();

        } else if (event.getSource() == addPatient_btn) {

            home_form.setVisible(false);
            addPatient_form.setVisible(true);
            salary_form.setVisible(false);

            addPatient_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            home_btn.setStyle("-fx-background-color:transparent");
            salary_btn.setStyle("-fx-background-color:transparent");
        } else if (event.getSource() == salary_btn) {

            home_form.setVisible(false);
            addPatient_form.setVisible(false);
            salary_form.setVisible(true);

            salary_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            home_btn.setStyle("-fx-background-color:transparent");
            addPatient_btn.setStyle("-fx-background-color:transparent");

            patientDetailsShowListData();
        }
    }

    private double x = 0;
    private double y = 0;

    public void logout() {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Message");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");
        Optional<ButtonType> option = alert.showAndWait();
        try {
            if (option.get().equals(ButtonType.OK)) {

                logout_btn.getScene().getWindow().hide();
                Parent root = FXMLLoader.load(getClass().getResource("/application/Login.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(root);

                root.setOnMousePressed((MouseEvent event) -> {
                    x = event.getSceneX();
                    y = event.getSceneY();
                });

                root.setOnMouseDragged((MouseEvent event) -> {
                    stage.setX(event.getScreenX() - x);
                    stage.setY(event.getScreenY() - y);

                    stage.setOpacity(.8);
                });

                root.setOnMouseReleased((MouseEvent event) -> {
                    stage.setOpacity(1);
                });

                stage.initStyle(StageStyle.TRANSPARENT);

                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void close() {
        System.exit(0);
    }

    public void minimize() {
        Stage stage = (Stage) main_form.getScene().getWindow();
        stage.setIconified(true);
    }

    public void patientDetailsShowListData() {
        ObservableList<patientData> listData = patientDetailsListData();

        patientDetails_col_patientID.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        patientDetails_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        patientDetails_col_age.setCellValueFactory(new PropertyValueFactory<>("age"));
        patientDetails_col_bloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        patientDetails_col_prognosis.setCellValueFactory(new PropertyValueFactory<>("prognosis"));

        patientDetails_tableView.setItems(listData);
    }

    public ObservableList<patientData> patientDetailsListData() {
        if (!USE_DATABASE) {
            return testPatientList;
        }

        // ObservableList<patientData> listData = FXCollections.observableArrayList();
        // String sql = "SELECT * FROM patient";
        //
        // connect = database.connectDb();
        //
        // try {
        //     prepare = connect.prepareStatement(sql);
        //     result = prepare.executeQuery();
        //
        //     while (result.next()) {
        //         patientData patient = new patientData(
        //                 result.getInt("patientID"),
        //                 result.getString("name"),
        //                 result.getInt("age"),
        //                 result.getString("gender"),
        //                 result.getString("phoneNumber"),
        //                 result.getString("bloodGroup"),
        //                 result.getString("image"),
        //                 result.getDate("admissionDate").toLocalDate(),
        //                 result.getString("status"),
        //                 getOptionalString(result, "prognosis")
        //         );
        //         listData.add(patient);
        //     }
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        //
        // return listData;
        return FXCollections.observableArrayList();
    }

    public void patientDetailsSelect() {
        patientData selectedPatient = patientDetails_tableView.getSelectionModel().getSelectedItem();
        int selectedIndex = patientDetails_tableView.getSelectionModel().getSelectedIndex();

        if (selectedPatient == null || selectedIndex < 0) {
            return;
        }

        patientDetails_patientID.setText(String.valueOf(selectedPatient.getPatientId()));
        patientDetails_name.setText(selectedPatient.getName());
        patientDetails_age.setText(String.valueOf(selectedPatient.getAge()));
        patientDetails_bloodGroup.setText(selectedPatient.getBloodGroup());
        patientDetails_prognosis.setText(selectedPatient.getPrognosis());
    }

    public void patientDetailsUpdate() {
        if (patientDetails_patientID.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter Patient ID.");
            return;
        }

        try {
            int patientId = Integer.parseInt(patientDetails_patientID.getText());
            String prognosisValue = patientDetails_prognosis.getText();

            if (!USE_DATABASE) {
                int existingIndex = findTestPatientIndex(patientId);
                if (existingIndex < 0) {
                    showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                    return;
                }
                patientData current = testPatientList.get(existingIndex);
                patientData updated = new patientData(
                        current.getPatientId(),
                        current.getName(),
                        current.getAge(),
                        current.getGender(),
                        current.getPhoneNum(),
                        current.getBloodGroup(),
                        current.getImage(),
                        current.getAdmissionDate(),
                        current.getStatus(),
                        prognosisValue
                );
                testPatientList.set(existingIndex, updated);
            } else {
                if (!hasPrognosisColumn) {
                    showAlert(Alert.AlertType.ERROR, "Database is missing prognosis column.");
                    return;
                }

                // String sql = "UPDATE patient SET prognosis = ? WHERE patientID = ?";
                //
                // connect = database.connectDb();
                //
                // prepare = connect.prepareStatement(sql);
                // prepare.setString(1, prognosisValue);
                // prepare.setInt(2, patientId);
                // prepare.executeUpdate();
            }

            showAlert(Alert.AlertType.INFORMATION, "Patient prognosis updated.");
            patientDetailsShowListData();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID must be a number.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update prognosis: " + e.getMessage());
        }
    }

    public void patientDetailsReset() {
        patientDetails_patientID.setText("");
        patientDetails_name.setText("");
        patientDetails_age.setText("");
        patientDetails_bloodGroup.setText("");
        patientDetails_prognosis.setText("");
    }

    private String storeImage(File file) throws Exception {
        String filename = UUID.randomUUID().toString().replace("-", "");
        String extension = getFileExtension(file.getName());
        if (!extension.isEmpty()) {
            filename = filename + "." + extension;
        }

        Path dir = Paths.get(IMAGE_STORAGE_DIR);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1);
    }

    private String resolveImagePath(String storedPath) {
        if (storedPath == null || storedPath.isEmpty()) {
            return null;
        }
        Path path = Paths.get(storedPath);
        if (path.isAbsolute()) {
            return path.toString();
        }
        Path base = Paths.get(IMAGE_STORAGE_DIR);
        if (path.startsWith(base)) {
            return path.toAbsolutePath().toString();
        }
        return base.resolve(path).toAbsolutePath().toString();
    }

    private boolean checkColumnExists(String table, String column) {
        try (Connection connection = database.connectDb()) {
            if (connection == null) {
                return false;
            }
            try (ResultSet columns = connection.getMetaData().getColumns(null, null, table, column)) {
                return columns.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private String getOptionalString(ResultSet resultSet, String column) {
        try {
            resultSet.findColumn(column);
            return resultSet.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private int findTestPatientIndex(int patientId) {
        for (int i = 0; i < testPatientList.size(); i++) {
            if (testPatientList.get(i).getPatientId() == patientId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        username.setText("Admin");
        defaultNav();

        home_form.setVisible(true);
        addPatient_form.setVisible(false);
        salary_form.setVisible(false);

        homePatientTotalPatients();
        homeTotalInactive();
        homeChart();

        addPatientShowListData();
        patientDetailsShowListData();

        hasPrognosisColumn = USE_DATABASE && checkColumnExists("patient", "prognosis");

        addPatientGenderList();
        addPatientBloodGroupList();
        addPatientStatusList();

        addPatientSearch();
    }

}
