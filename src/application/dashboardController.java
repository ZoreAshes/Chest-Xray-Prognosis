package application;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        String checkSql = "SELECT patientID FROM patient WHERE patientID = ?";
        String insertSql = "INSERT INTO patient "
                + "(patientID, name, age, gender, phoneNumber, bloodGroup, image, admissionDate, status) "
                + "VALUES(?,?,?,?,?,?,?,?,?)";

        connect = database.connectDb();

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

            prepare = connect.prepareStatement(checkSql);
            prepare.setInt(1, patientId);
            result = prepare.executeQuery();

            if (result.next()) {
                showAlert(Alert.AlertType.ERROR, "Patient ID already exists!");
                return;
            }

            prepare = connect.prepareStatement(insertSql);
            prepare.setInt(1, patientId);
            prepare.setString(2, addPatient_name.getText());
            prepare.setInt(3, age);
            prepare.setString(4, addPatient_gender.getValue().toString());
            prepare.setString(5, addPatient_phoneNumber.getText());
            prepare.setString(6, addPatient_bloodGroup.getValue().toString());
            prepare.setString(7, getData.path);
            prepare.setString(9, addPatient_status.getValue().toString());
            prepare.setDate(8, sqlDate);

            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Patient Added Successfully!");

            addPatientShowListData();
            addPatientReset();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID and Age must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addPatientUpdate() {

        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        String updateSql = "UPDATE patient SET "
                + "name = ?, "
                + "age = ?, "
                + "gender = ?, "
                + "phoneNumber = ?, "
                + "bloodGroup = ?, "
                + "image = ?, "
                + "admissionDate = ?, "
                + "status = ? "
                + "WHERE patientID = ?";

        connect = database.connectDb();

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

                prepare = connect.prepareStatement(updateSql);

                prepare.setString(1, addPatient_name.getText());
                prepare.setInt(2, age);
                prepare.setString(3, addPatient_gender.getValue().toString());
                prepare.setString(4, addPatient_phoneNumber.getText());
                prepare.setString(5, addPatient_bloodGroup.getValue().toString());
                prepare.setString(6, getData.path);
                prepare.setDate(7, sqlDate);
                prepare.setString(8, addPatient_status.getValue().toString());
                prepare.setInt(9, patientId);

                prepare.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Patient Updated Successfully!");

                addPatientShowListData();
                addPatientReset();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID and Age must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addPatientDelete() {

        String deleteSql = "DELETE FROM patient WHERE patientID = ?";

        connect = database.connectDb();

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

                prepare = connect.prepareStatement(deleteSql);
                prepare.setInt(1, patientId);
                prepare.executeUpdate();

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
    }

    public void addPatientInsertImage() {

        FileChooser open = new FileChooser();

        open.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = open.showOpenDialog(main_form.getScene().getWindow());

        if (file != null) {
            getData.path = file.getAbsolutePath();

            Image image = new Image(file.toURI().toString(), 101, 127, false, true);
            addPatient_image.setImage(image);
        }
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

        ObservableList<String> listData = FXCollections.observableArrayList(
                "Active",
                "Inactive"
        );

        addPatient_status.getItems().clear();   // clear first
        addPatient_status.setItems(listData);   // set only once

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

        ObservableList<patientData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM patient";

        connect = database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            while (result.next()) {

                patientData patient = new patientData(
                        result.getInt("patientID"),
                        result.getString("name"),
                        result.getInt("age"),
                        result.getString("gender"),
                        result.getString("phoneNumber"),
                        result.getString("bloodGroup"),
                        result.getString("image"),
                        result.getDate("admissionDate").toLocalDate(),
                        result.getString("status")
                );

                listData.add(patient);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listData;
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

        getData.path = selectedPatient.getImage();
        String uri = "file:" + selectedPatient.getImage();
        Image image = new Image(uri, 101, 127, false, true);
        addPatient_image.setImage(image);
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

            home_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            addPatient_btn.setStyle("-fx-background-color:transparent");

            homePatientTotalPatients();
            homeTotalInactive();
            homeChart();

        } else if (event.getSource() == addPatient_btn) {

            home_form.setVisible(false);
            addPatient_form.setVisible(true);

            addPatient_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            home_btn.setStyle("-fx-background-color:transparent");
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        username.setText("Admin");
        defaultNav();

        home_form.setVisible(true);
        addPatient_form.setVisible(false);

        homePatientTotalPatients();
        homeTotalInactive();
        homeChart();

        addPatientShowListData();

        addPatientGenderList();
        addPatientBloodGroupList();
        addPatientStatusList();

        addPatientSearch();
    }

}
