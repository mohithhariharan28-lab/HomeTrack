package hometrack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Asset implements ReminderTrackable {
    private int id;
    private final int userId;
    private String name;
    private LocalDate purchaseDate;
    private double purchasePrice;
    private String location;
    private LocalDate reminderDate;
    private String reminderNote;
    private String warrantyProvider;
    private LocalDate warrantyEndDate;
    private String insuranceProvider;
    private LocalDate insuranceEndDate;
    private double insurancePremium;
    private String emiLender;
    private double emiMonthlyAmount;
    private LocalDate emiNextDueDate;
    private final List<LocalDate> trackedDates = new ArrayList<>();

    protected Asset(int id, int userId, String name, LocalDate purchaseDate, double purchasePrice,
                    String location, LocalDate reminderDate, String reminderNote) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.location = location;
        this.reminderDate = reminderDate;
        this.reminderNote = reminderNote;
    }

    public abstract String getCategory();

    @Override
    public LocalDate getNextReminderDate() {
        return trackedDates.stream().min(LocalDate::compareTo).orElse(null);
    }

    @Override
    public String getReminderLabel() {
        return getCategory() + " reminder";
    }

    public void refreshTrackedDates() {
        trackedDates.clear();
        addDate(reminderDate);
        addDate(warrantyEndDate);
        addDate(insuranceEndDate);
        addDate(emiNextDueDate);
    }

    private void addDate(LocalDate date) {
        if (date != null) trackedDates.add(date);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getReminderDate() { return reminderDate; }
    public void setReminderDate(LocalDate reminderDate) { this.reminderDate = reminderDate; }
    public String getReminderNote() { return reminderNote; }
    public void setReminderNote(String reminderNote) { this.reminderNote = reminderNote; }
    public String getWarrantyProvider() { return warrantyProvider; }
    public void setWarrantyProvider(String value) { warrantyProvider = value; }
    public LocalDate getWarrantyEndDate() { return warrantyEndDate; }
    public void setWarrantyEndDate(LocalDate value) { warrantyEndDate = value; }
    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String value) { insuranceProvider = value; }
    public LocalDate getInsuranceEndDate() { return insuranceEndDate; }
    public void setInsuranceEndDate(LocalDate value) { insuranceEndDate = value; }
    public double getInsurancePremium() { return insurancePremium; }
    public void setInsurancePremium(double value) { insurancePremium = value; }
    public String getEmiLender() { return emiLender; }
    public void setEmiLender(String value) { emiLender = value; }
    public double getEmiMonthlyAmount() { return emiMonthlyAmount; }
    public void setEmiMonthlyAmount(double value) { emiMonthlyAmount = value; }
    public LocalDate getEmiNextDueDate() { return emiNextDueDate; }
    public void setEmiNextDueDate(LocalDate value) { emiNextDueDate = value; }
}
