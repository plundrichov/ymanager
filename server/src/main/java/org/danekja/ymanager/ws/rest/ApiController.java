package org.danekja.ymanager.ws.rest;

import org.apache.commons.lang3.StringUtils;
import org.danekja.ymanager.business.FileService;
import org.danekja.ymanager.business.Manager;
import org.danekja.ymanager.dto.*;
import org.danekja.ymanager.util.localization.Language;
import org.danekja.ymanager.util.localization.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    private final Manager manager;

    private final FileService fileService;

    @Autowired
    public ApiController(Manager manager, FileService fileService) {
        this.manager = manager;
        this.fileService = fileService;
    }

    private ResponseEntity sendError(Integer errorCode, String messageKey, Language language) {
        String localizedMessage = Message.getString(language, messageKey);
        Map<String, String> result = new HashMap<>();
        result.put("error", errorCode.toString());
        result.put("message", localizedMessage);
        return ResponseEntity.status(errorCode).contentType(MediaType.APPLICATION_JSON).body(result);
    }

    private <T> ResponseEntity handle(Language language, RESTInvokeHandler<T> handler) {
        try {
            handler.invoke();
            return ok(OK);
        } catch (RESTFullException e) {
            log.error(e.getMessage());
            return sendError(400, e.getLocalizedMessage(), language);
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendError(401, e.getMessage(), language);
        }
    }

    private <T> ResponseEntity handle(Language language, RESTGetHandler<T> handler) {
        return handle(language, handler, null, null);
    }

    private <T> ResponseEntity handle(Language language, RESTGetHandler<T> handler, Function<T, String[]> header, Function<T, Object> bodyValue) {
        try {
            T result = handler.get();

            ResponseEntity.BodyBuilder response = ResponseEntity.ok();

            if (header != null) {
                String[] headers = header.apply(result);

                if (headers.length > 1) {
                    response.header(headers[0], Arrays.copyOfRange(headers, 1, headers.length - 1));
                } else if (headers.length == 1) {
                    response.header(headers[0]);
                }
            }

            return response.body(bodyValue != null ? bodyValue.apply(result) : result);

        } catch (RESTFullException e) {
            log.error(e.getMessage());
            return sendError(400, e.getLocalizedMessage(), language);
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendError(401, "rest.exception.generic", language);
        }
    }

    private Long getUserId(String id) {
        // TODO verify permission
        if (id.toLowerCase().equals("me")) {
            return 1L;
        } else if (StringUtils.isNumeric(id)) {
            return Long.valueOf(id);
        } else {
            throw new IllegalArgumentException("User not found.");
        }
    }

    private Long getUserId(Long id) {
        return id == null ? getUserId("me") : id;
    }

    // *********************** GET ****************************

    @RequestMapping(value = "/users", method=GET)
    public ResponseEntity users(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "status", required = false) String status)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.getUsers(Status.getStatus(status))
        );
    }

    @RequestMapping(value = "/users/requests/vacation", method=GET)
    public ResponseEntity usersRequestsVacation(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "status", required = false) String status)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.getVacationRequests(Status.getStatus(status))
        );
    }

    @RequestMapping(value = "/users/requests/authorization", method=GET)
    public ResponseEntity userRequestsAuthorization(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "status", required = false) String status)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.getAuthorizationRequests(Status.getStatus(status))
         );
    }

    @RequestMapping(value = "/user/{id}/profile", method=GET)
    public ResponseEntity userProfile(
            @PathVariable("id") String id,
            @RequestParam(value = "lang", required = false) String lang)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.getUserProfile(getUserId(id))
        );
    }

    @RequestMapping(value = "/user/{id}/calendar", method=GET)
    public ResponseEntity userCalendar(
            @PathVariable("id") String id,
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "from", required = true) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "status", required = false) String status)
    {
        return handle(Language.getLanguage(lang), () -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate fromDate = LocalDate.parse(from, formatter);
            LocalDate toDate = to != null ? LocalDate.parse(to, formatter) : null;
            return manager.getUserCalendar(getUserId(id), fromDate, toDate, Status.getStatus(status));
        });
    }

    @RequestMapping(value = "/settings", method=GET)
    public ResponseEntity settings(
            @RequestParam(value = "lang", required = false) String lang)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.getDefaultSettings()
        );
    }

    // *********************** POST ****************************

    @RequestMapping(value = "/settings", method=POST)
    public ResponseEntity settings(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestBody DefaultSettings settings)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.createSettings(settings)
        );
    }

    @RequestMapping(value = "/user/calendar/create", method=POST)
    public ResponseEntity userCalendarCreate(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestBody VacationDay vacationDay)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.createVacation(getUserId("me"), vacationDay)
        );
    }

    // *********************** PUT ****************************


    @RequestMapping(value = "/user/settings", method=PUT)
    public ResponseEntity userSettings(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestBody UserSettings settings)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.changeSettings(getUserId("me"), settings)
        );
    }

    @RequestMapping(value = "/user/calendar/edit", method=PUT)
    public ResponseEntity userCalendarEdit(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestBody VacationDay vacationDay)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.changeVacation(getUserId("me"), vacationDay)
        );
    }

    @RequestMapping(value = "/user/requests", method=PUT)
    public ResponseEntity userRequests(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "type", required = true) String type,
            @RequestBody BasicRequest request)
    {
        return handle(Language.getLanguage(lang), () ->
                manager.changeRequest(RequestType.getType(type), request)
        );
    }

    // *********************** DELETE ****************************

    @RequestMapping(value = "/calendar/{id}/delete", method=DELETE)
    public ResponseEntity calendarDelete(
            @PathVariable("id") String id,
            @RequestParam(value = "lang", required = false) String lang)
    {
        return handle(Language.getLanguage(lang), () ->
            manager.deleteVacation(getUserId("me"), StringUtils.isNumeric(id) ? Long.parseLong(id) : -1)
        );
    }

    // *********************** FILE ****************************

    @RequestMapping(value = "/import/xls", method=POST)
    public ResponseEntity importXLSFile(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam("file") MultipartFile file)
    {
        return handle(Language.getLanguage(lang), () ->
            fileService.parseXLSFile(file.getOriginalFilename(), file.getBytes())
        );
    }

    @RequestMapping(value = "/export/pdf", method=GET)
    public ResponseEntity exportPDFFile(
            @RequestParam(value = "lang", required = false) String lang)
    {
        return handle(Language.getLanguage(lang),
                () -> fileService.createPDFFile(),
                (res) -> new String[]{HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getName() + "\""},
                (res) -> res.getBytes()
        );
    }
}