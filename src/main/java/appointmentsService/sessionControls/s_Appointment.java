package appointmentsService.sessionControls;

import com.appointmentsService.model.Appointment;
import com.appointmentsService.sessionControls.sessionParentClass;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class s_Appointment extends sessionParentClass {
    void createAppointment(String _Title,
                           boolean _isPublic,
                           int _Semester,
                           LocalDateTime _startTimeDate,
                           LocalDateTime _endTimeDate,
                           int _CourseID,
                           int _FacultyID,
                           int _LocID,
                           int _OrganizerID) {

        getSessionFactory();
        try (Session session = sessionFactory.openSession()) {

            Transaction tx = null;
            try {
                tx = session.beginTransaction();

                Appointment appointment = new Appointment();
                appointment.setTitle(_Title);
                appointment.setPublicMode(_isPublic);
                appointment.setSemester(_Semester);
                appointment.setNewStart(_startTimeDate);
                appointment.setNewEnd(_endTimeDate);

                appointment.setCourseID(_CourseID);
                appointment.setFacultyID(_FacultyID);
                appointment.setLocationID(_LocID);
                appointment.setPersonID(_OrganizerID);

                session.persist(appointment);

                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    //---------------------------------------------------------------------

    void changeAppointment(int _ID,
                           String _Title,
                           boolean _isPublic,
                           int _Semester,
                           LocalDateTime _startTimeDate,
                           LocalDateTime _endTimeDate,
                           int _CourseID,
                           int _FacultyID,
                           int _LocID,
                           int _OrganizerID) {

        getSessionFactory();
        try (Session session = sessionFactory.openSession()) {

            Transaction tx = null;
            try {
                tx = session.beginTransaction();

                String hql = "FROM Appointments a WHERE a.ID = :_ID";
                Query query = session.createQuery(hql, Appointment.class);
                if (_Title != null) query.setParameter("title", _Title);
                query.setParameter("ispublic", _isPublic);
                query.setParameter("semester", _Semester);
                query.setParameter("startdatetime", _startTimeDate);
                query.setParameter("enddatetime", _endTimeDate);
                query.setParameter("course_id", _CourseID);
                query.setParameter("faculty_id", _FacultyID);
                query.setParameter("location_id", _LocID);
                query.setParameter("organizer_id", _OrganizerID);

                Optional<Appointment> first = query.getResultList().stream().findFirst();
                Appointment appointment = first.get();
                appointment.setTitle(_Title);
                appointment.setSemester(_Semester);
                appointment.setNewStart(_startTimeDate);
                appointment.setNewEnd(_endTimeDate);
                appointment.setCourseID(_CourseID);
                appointment.setFacultyID(_FacultyID);
                appointment.setLocationID(_LocID);
                appointment.setPersonID(_OrganizerID);
                session.persist(appointment);

                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    //---------------------------------------------------------------------

    void printAllAppointments(){
        getSessionFactory();
        try (Session session = sessionFactory.openSession()) {

            Transaction tx = null;
            List<Appointment> appointments = session.createQuery("from Appointments", Appointment.class).list();
            for (Appointment a : appointments) {
                System.out.println(a.toString());
            }
        }
    }
    /** Search for a public appointments by certain criteria
     * @param searchTerm search term included in the title of the appointment
     * @param fromDate define the start of time interval, can only be used in conjunction with "toDate" (see below)
     * @param toDate define the end of time interval, can only be used in conjunction with "fromDate"
     * @param courseID show appointments from one course
     * @param facultyID show appointments from one faculty
     * @param locationID show appointments from one location
     * @param personId show appointments from one organizer
     * @return list of type <Appointment> containing matching appointments
     */
    public List<Appointment> searchAppointments(String searchTerm, Integer semester, Timestamp fromDate, Timestamp toDate, BigInteger courseID,
                                                BigInteger facultyID, BigInteger locationID, BigInteger personId) {
        getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Appointment> cr = criteriaBuilder.createQuery(Appointment.class);
            Root<Appointment> root = cr.from(Appointment.class);
            ArrayList<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(criteriaBuilder.isTrue(root.get("ISPUBLIC")));
            if (searchTerm != null) {
                predicates.add(criteriaBuilder.like(root.get("TITLE"), "%" + searchTerm + "%"));
            }
            if (fromDate != null  && toDate != null) {
                predicates.add(criteriaBuilder.or(criteriaBuilder.lessThan(root.get("STARTDATETIME"), toDate), criteriaBuilder.greaterThan(root.get("ENDDATETIME"), fromDate )));
            }
            if (semester != null) {
                predicates.add(criteriaBuilder.equal(root.get("SEMESTER"), semester));
            }
            if (courseID != null) {
                predicates.add(criteriaBuilder.equal(root.get("COURSE_ID"), courseID));
            }
            if (facultyID != null) {
                predicates.add(criteriaBuilder.equal(root.get("FACULTY_ID"), facultyID));
            }
            if (locationID != null) {
                predicates.add(criteriaBuilder.equal(root.get("LOCATION_ID"), locationID));
            }
            if (personId != null) {
                predicates.add(criteriaBuilder.equal(root.get("PERSON_ID"), personId));
            }
            Predicate[] array = predicates.toArray(new Predicate[0]);
            cr.select(root).where(array);
            return session.createQuery(cr).list();

        }
}