package co.tz.sheriaconnectapi.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Index {

    @GetMapping("/")
    public String index(){
        return "Connected Succesfully";
    }
}
