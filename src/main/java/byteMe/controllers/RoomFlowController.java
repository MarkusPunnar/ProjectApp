package byteMe.controllers;

import byteMe.model.ByteMeElement;
import byteMe.services.GameInstanceService;
import byteMe.services.RoomFlowRepsitory;
import byteMe.services.RoomStartupRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/room")
public class RoomFlowController {

    @Value("${instanceFileLocation}")
    private String pictureLocation;
    private Jdbi jdbi;
    private GameInstanceService gameInstanceService;

    @Autowired
    public RoomFlowController(Jdbi jdbi, GameInstanceService gameInstanceService) {
        this.jdbi = jdbi;
        this.gameInstanceService = gameInstanceService;
    }

    @RequestMapping("/{instanceID}/displayElement/{elementNumber}")
    public String createDisplay(@PathVariable("instanceID") String instanceIDAsString,
                                @PathVariable("elementNumber") String elementNumberAsString, Model model) {
        Integer instanceID = Integer.valueOf(instanceIDAsString);
        model.addAttribute("roomID", instanceID);
        Integer elementNumber = Integer.valueOf(elementNumberAsString);
        model.addAttribute("elementNumber", elementNumber);
        return jdbi.inTransaction(handle -> {
            RoomFlowRepsitory roomFlowRepsitory = handle.attach(RoomFlowRepsitory.class);
            List<ByteMeElement> elementList = roomFlowRepsitory.getAllElementsByRoom(instanceID);
            if (elementNumber > elementList.size()) {
                return "redirect:/room/" + instanceIDAsString + "/summary";
            }
            ByteMeElement currentElement = elementList.get(elementNumber - 1);
            if (currentElement.getElementType().toLowerCase().equals("text")) {
                model.addAttribute("elementContent", currentElement.getElementContent());
            }
            model.addAttribute("elementType", currentElement.getElementType());
            model.addAttribute("elementID", currentElement.getElementID());
            return "display";
        });
    }

    @RequestMapping(value = "/{instanceID}/getImage/{elementNumber}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public FileSystemResource showImage(@PathVariable("instanceID") String instanceIDAsString,
                                        @PathVariable("elementNumber") String elementNumberAsString) {
        Integer elementNumber = Integer.valueOf(elementNumberAsString);
        String pictureFileName = instanceIDAsString + "_" + (elementNumber - 1) + ".png";
        String picturePath = pictureLocation + pictureFileName;
        return new FileSystemResource(picturePath);
    }

    @RequestMapping(value = "/{instanceID}/gradeElement/{elementNumber}", method = RequestMethod.POST)
    public String gradeElement(@PathVariable("instanceID") String instanceIDAsString,
                               @PathVariable("elementNumber") String elementNumberAsString,
                               @ModelAttribute("grade") Integer grade,
                               @ModelAttribute("current") String elementIDAsString) {
        int elementNumber = Integer.valueOf(elementNumberAsString);
        int instanceID = Integer.valueOf(instanceIDAsString);
        int elementID = Integer.valueOf(elementIDAsString);
        return jdbi.inTransaction(handle -> {
            RoomFlowRepsitory roomFlowRepsitory = handle.attach(RoomFlowRepsitory.class);
            RoomStartupRepository roomStartupRepository = handle.attach(RoomStartupRepository.class);
            int gradeUser = roomStartupRepository.getUserID(gameInstanceService.getCurrentUsername(jdbi));
            roomFlowRepsitory.saveGrade(instanceID, elementID, grade, gradeUser);
            return "redirect:/room/" + instanceIDAsString + "/displayElement/" + (elementNumber + 1);
        });
    }

    @RequestMapping("/{instanceID}/summary")
    public String showSummary(@PathVariable("instanceID") String instanceIDAsString, Model model) {
        int instanceID = Integer.parseInt(instanceIDAsString);
        return jdbi.inTransaction(handle -> {
            RoomFlowRepsitory roomFlowRepsitory = handle.attach(RoomFlowRepsitory.class);
            RoomStartupRepository roomRepository = handle.attach(RoomStartupRepository.class);
            String hostname = roomRepository.getHostName(instanceID);
            int elementCount = roomFlowRepsitory.getAllElementsByRoom(instanceID).size();
            int secondsFromCreation = roomFlowRepsitory.getSecondsFromCreation(instanceID);
            int secondsToClosure = 3600 - secondsFromCreation;
            String creationTimeString = gameInstanceService.formatTimeFromCreation(secondsFromCreation);
            String closureTimeString = gameInstanceService.formatTimeFromCreation(secondsToClosure);
            model.addAttribute("instanceID", instanceID);
            model.addAttribute("fromCreation", creationTimeString);
            model.addAttribute("toClosure", closureTimeString);
            model.addAttribute("hostname", hostname);
            model.addAttribute("elementCount", elementCount);
            model.addAttribute("userCount", roomFlowRepsitory.getGradedUserCount(instanceID));
            return "summary";
        });
    }


    @RequestMapping("/{instanceID}/timestamps")
    @ResponseBody
    public Map<String, String> getTimestamps(@PathVariable("instanceID") String instanceIDAsString) {
        int instanceID = Integer.parseInt(instanceIDAsString);
        return jdbi.inTransaction(handle -> {
            Map<String, String> timestampMap = new HashMap<>();
            RoomFlowRepsitory roomFlowRepsitory = handle.attach(RoomFlowRepsitory.class);
            int secondsFromCreation = roomFlowRepsitory.getSecondsFromCreation(instanceID);
            int secondsToClosure = 3600 - secondsFromCreation;
            String timeFromCreation = gameInstanceService.formatTimeFromCreation(secondsFromCreation);
            String timeToClosure = gameInstanceService.formatTimeFromCreation(secondsToClosure);
            timestampMap.put("creation", timeFromCreation);
            timestampMap.put("closure", timeToClosure);
            return timestampMap;
        });
    }
}
