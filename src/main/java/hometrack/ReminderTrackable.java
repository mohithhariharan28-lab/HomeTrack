package hometrack;

import java.time.LocalDate;

public interface ReminderTrackable {
    LocalDate getNextReminderDate();
    String getReminderLabel();
}
