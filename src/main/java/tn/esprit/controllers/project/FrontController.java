package tn.esprit.controllers.project;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import tn.esprit.entities.project.Project;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.project.ProjectService;
import tn.esprit.services.group.GroupService;

import netscape.javascript.JSObject;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.IOException;

public class FrontController {

    @FXML 
    private WebView calendarWebView;

    private WebEngine webEngine;

    private final String JITSI_LINK = "https://meet.jit.si/LeadingCoordinatorsHealFinally";

    @FXML
    public void initialize() {
        if (calendarWebView == null) {
            throw new IllegalStateException("WebView non initialisé");
        }

        webEngine = calendarWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        loadCustomCalendar();
    }

    private void loadCustomCalendar() {
        try {
            DateTimeFormatter jsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            List<Project> projects = ProjectService.getInstance().getAll();
            List<GroupStudent> groups = GroupService.getInstance().getAll();

            StringBuilder eventsJson = new StringBuilder("[");

            for (Project project : projects) {
                if (project.getDeadline() != null) {
                    eventsJson.append("{")
                             .append("title: 'Deadline: ").append(escapeJs(project.getTitre())).append("',")
                             .append("start: '").append(project.getDeadline().format(jsFormatter)).append("',")
                             .append("className: 'fc-event-deadline',")
                             .append("allDay: true")
                             .append("},");
                }
            }

            for (GroupStudent group : groups) {
                if (group.getMeetingDate() != null) {
                    eventsJson.append("{")
                             .append("title: 'Réunion: ").append(escapeJs(group.getName())).append("',")
                             .append("start: '").append(group.getMeetingDate().format(jsFormatter)).append("T10:00:00',")
                             .append("end: '").append(group.getMeetingDate().format(jsFormatter)).append("T12:00:00',")
                             .append("className: 'fc-event-meeting',")
                             .append("extendedProps: { ")
                             .append("  isMeeting: true,")
                             .append("  meetingLink: '").append(JITSI_LINK).append("'")
                             .append("}")
                             .append("},");
                }
            }

            if (!eventsJson.toString().endsWith("[")) {
                eventsJson.deleteCharAt(eventsJson.length() - 1); // remove last comma
            }

            eventsJson.append("]");

            String htmlContent = createHtmlContent(eventsJson.toString());
            webEngine.loadContent(htmlContent);

            // 👇 Injecter le bridge Java vers JS après chargement
            webEngine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaObject", new JavaBridge());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            webEngine.loadContent("<h2 style='color:red'>Erreur de chargement du calendrier</h2>");
        }
    }

    private String createHtmlContent(String eventsJson) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='UTF-8'>
            <script src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.9/index.global.min.js'></script>
            <style>
                body { margin: 0; padding: 10px; font-family: Arial; background: #f5f7fa; }
                #calendar { 
                    max-width: 100%; 
                    margin: 0 auto;
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    padding: 10px;
                }
                .fc-event-deadline { 
                    background-color: #ff6b6b !important; 
                    border-color: #d32f2f !important;
                    color: white !important;
                }
                .fc-event-meeting { 
                    background-color: #4dabf7 !important; 
                    border-color: #1971c2 !important;
                    color: white !important;
                    cursor: pointer !important;
                }
                .fc-toolbar-title {
                    font-size: 1.4em;
                    color: #2c3e50;
                    font-weight: 600;
                }
                .fc-button {
                    background: #4dabf7 !important;
                    border: none !important;
                    color: white !important;
                }
            </style>
        </head>
        <body>
            <div id='calendar'></div>
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    const calendarEl = document.getElementById('calendar');
                    const calendar = new FullCalendar.Calendar(calendarEl, {
                        initialView: 'dayGridMonth',
                        headerToolbar: {
                            left: 'prev,next today',
                            center: 'title',
                            right: 'dayGridMonth,timeGridWeek,timeGridDay'
                        },
                        eventClick: function(info) {
                            if (info.event.extendedProps.isMeeting) {
                                const roomUrl = info.event.extendedProps.meetingLink;
                                const roomName = roomUrl.split('/').pop();
                                if (window.javaObject) {
                                    window.javaObject.openJitsiFromJS(roomName);
                                } else {
                                    alert("Bridge Java non disponible");
                                }
                            }
                        },
                        events: """ + eventsJson + """
                    });
                    calendar.render();
                });
            </script>
        </body>
        </html>
        """;
    }

    private String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }

    // ✅ Méthode appelée par le bridge Java exposé à JS
    public static void openJitsiStatic(String roomName) {
        try {
            String jitsiUrl = "https://meet.jit.si/" + roomName;
            String[] chromePaths = {
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe"
            };
            for (String path : chromePaths) {
                if (new java.io.File(path).exists()) {
                    new ProcessBuilder(path, "--new-window", "--app=" + jitsiUrl).start();
                    return;
                }
            }
            System.err.println("Google Chrome non trouvé");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Bridge pour communication JS → Java
    public class JavaBridge {
        public void openJitsiFromJS(String roomName) {
            System.out.println("JS a demandé d'ouvrir : " + roomName);
            FrontController.openJitsiStatic(roomName);
        }
    }
}
