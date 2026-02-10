package tz.co.miugro.Abstractions;

import org.springframework.http.ResponseEntity;

public interface Command <I,O>{
    public ResponseEntity<O> execute(I input);
}
