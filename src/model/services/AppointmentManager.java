package model.services;

import model.Appointment;
import model.DataAccessLayer;

public class AppointmentManager {
    private final DataAccessLayer dal;

    public AppointmentManager(DataAccessLayer dal) {
        this.dal = dal;
    }

    public void bookAppointment(Appointment a) {
        dal.saveNewAppointment(a);
    }

    public void reschedule(String id, String date, String time) {
        dal.updateAppointment(id, date + " " + time, "Scheduled");
    }

    public void cancel(String id) {
        dal.cancelAppointment(id);
    }
}