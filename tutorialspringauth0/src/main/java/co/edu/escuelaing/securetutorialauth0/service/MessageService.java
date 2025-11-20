package co.edu.escuelaing.securetutorialauth0.service;

import co.edu.escuelaing.securetutorialauth0.model.MessageEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Service
public class MessageService {

    private final Deque<MessageEntry> store = new LinkedList<>();
    private final int MAX = 10;

    public synchronized void add(MessageEntry entry) {
        store.addLast(entry);
        while (store.size() > MAX) {
            store.removeFirst();
        }
    }

    public synchronized List<MessageEntry> lastMessages() {
        return new ArrayList<>(store);
    }

    /**
     * Devuelve una representación de texto vertical (uno por línea) para mostrar 
     * en el backend si se solicita text/plain.
     */
    public synchronized String lastMessagesAsPlainText() {
        StringBuilder sb = new StringBuilder();
        for (MessageEntry e : store) {
            sb.append(e.getMessage())
              .append(" (")
              .append(e.getClientIp() == null ? "unknown-ip" : e.getClientIp())
              .append(")")
              .append(" -> ")
              .append(e.getTimestamp())
              .append("\n");
        }
        return sb.toString();
    }
}