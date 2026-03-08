package application;

import java.time.LocalDate;

public class patientData {

    private Integer patientId;
    private String name;
    private Integer age;
    private String gender;
    private String phoneNumber;
    private String bloodGroup;
    private String imagePath;
    private LocalDate admissionDate;
    private String status;

    public patientData(int patientId, String name, int age, String gender, String phoneNumber, String bloodGroup, String imagePath, LocalDate admissionDate, String status) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.bloodGroup = bloodGroup;
        this.imagePath = imagePath;
        this.admissionDate = admissionDate;
        this.status = status;
    }

    public Integer getPatientId() { return patientId; }
    public String getName() { return name; }
    public Integer getAge() { return age; }
    public String getGender() { return gender; }
    public String getPhoneNum() { return phoneNumber; }
    public String getBloodGroup() { return bloodGroup; }
    public String getImage() { return imagePath; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public String getStatus() { return status; }
}
