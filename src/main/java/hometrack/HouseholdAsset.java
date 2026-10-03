package hometrack;

import java.time.LocalDate;

public final class HouseholdAsset extends Asset {
    public HouseholdAsset(int id, int userId, String name, LocalDate purchaseDate, double purchasePrice,
                          String location, LocalDate reminderDate, String reminderNote) {
        super(id, userId, name, purchaseDate, purchasePrice, location, reminderDate, reminderNote);
    }

    @Override
    public String getCategory() { return "Home"; }

    @Override
    public String getReminderLabel() { return "Home maintenance"; }
}
