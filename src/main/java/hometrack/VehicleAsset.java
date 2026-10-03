package hometrack;

import java.time.LocalDate;

public final class VehicleAsset extends Asset {
    public VehicleAsset(int id, int userId, String name, LocalDate purchaseDate, double purchasePrice,
                        String location, LocalDate reminderDate, String reminderNote) {
        super(id, userId, name, purchaseDate, purchasePrice, location, reminderDate, reminderNote);
    }

    @Override
    public String getCategory() { return "Vehicle"; }

    @Override
    public String getReminderLabel() { return "Vehicle service"; }
}
