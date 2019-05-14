package cz.zcu.yamanager.manager;

import cz.zcu.yamanager.dto.*;
import cz.zcu.yamanager.ws.rest.RESTFullException;

import java.time.LocalDate;
import java.util.List;

public interface Manager {

    List<BasicProfileUser> getUsers(Status status) throws RESTFullException;

    List<VacationRequest> getVacationRequests(Status status) throws RESTFullException;

    List<AuthorizationRequest> getAuthorizationRequests(Status status) throws RESTFullException;

    FullUserProfile getUserProfile(Long userId) throws RESTFullException;

    DefaultSettings getDefaultSettings() throws RESTFullException;

    List<VacationDay> getUserCalendar(Long userId, LocalDate fromDate, LocalDate toDate, Status status) throws RESTFullException;

    void createSettings(DefaultSettings settings) throws RESTFullException;

    void createVacation(Long userId, VacationDay vacationDay) throws RESTFullException;

    void changeSettings(Long userId, UserSettings settings) throws RESTFullException;

    void changeVacation(Long userId, VacationDay vacationDay) throws RESTFullException;

    void changeRequest(RequestType type, BasicRequest request) throws RESTFullException;

    void deleteVacation(Long userId, Long vacationId) throws RESTFullException;
}
