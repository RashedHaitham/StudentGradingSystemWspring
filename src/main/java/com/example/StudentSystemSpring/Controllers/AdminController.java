package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import com.example.StudentSystemSpring.Util.PasswordHashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {
    private final DAO dao;

    @Autowired
    public AdminController(DAO dao) {
        this.dao = dao;
    }

    @GetMapping("/crud")
    public String crud(@RequestParam("table") String table, Model model) {
        List<String> headers = dao.getTableColumns(table);
        List<String[]> rowsData = dao.getTableContent(table);
        model.addAttribute("table", table);
        model.addAttribute("headers", headers);
        model.addAttribute("rowsData", rowsData);
        return "crud";
    }

    @GetMapping("/createRecord")
    public String displayCreateRecordForm(@RequestParam String table,
                              Model model) {
        model.addAttribute("table", table);
        return "create_record";
    }

    @PostMapping("/createRecord")
    public String createRecord(@RequestParam String table,
                               @RequestParam(value = "user_id", required = false) String userId,
                               @RequestParam(value = "username", required = false) String username,
                               @RequestParam(value = "password", required = false) String password,
                               @RequestParam(value = "course_id", required = false) String courseId,
                               @RequestParam(value = "course_name", required = false) String courseName,
                               Model model) {
        Map<String, String> inputData = new HashMap<>();
        List<String> columns = dao.getTableColumns(table);
        model.addAttribute("table", table);

        if ("courses".equals(table)) {
            if (courseId == null || courseId.isEmpty() || courseName == null || courseName.isEmpty()) {
                model.addAttribute("errorMessage", "Course ID and Course Name must be provided.");
                return displayCreateRecordForm(table, model);
            }

            if (!courseId.matches("\\d{7}")) {
                model.addAttribute("errorMessage", "Invalid ID format. ID must be a 7-digit numeric value.");
                return displayCreateRecordForm(table, model);
            }
            if (dao.isIdExists(courseId, table)) {
                model.addAttribute("errorMessage", "Error: Course ID already exists. Please enter a different ID.");
                return displayCreateRecordForm(table, model);
            }

            inputData.put(columns.get(0), courseId);
            inputData.put("course_name", courseName);

        } else {
            if (userId == null || userId.isEmpty() || username == null || username.isEmpty() || password == null || password.isEmpty()) {
                model.addAttribute("errorMessage", "User ID, Username, and Password must be provided.");
                return displayCreateRecordForm(table, model);
            }

            if (!userId.matches("\\d{7}")) {
                model.addAttribute("errorMessage", "Invalid ID format. ID must be a 7-digit numeric value.");
                return displayCreateRecordForm(table, model);
            }
            if (dao.isIdExists(userId, table)) {
                model.addAttribute("errorMessage", "Error: ID already exists. Please enter a different ID.");
                return displayCreateRecordForm(table, model);
            }

            inputData.put(columns.get(0), userId);
            inputData.put("username", username);
            inputData.put("password", PasswordHashing.hashPassword(password));
        }

        boolean isAdded = dao.addRecord(table, inputData);
        if (isAdded) {
            model.addAttribute("successMessage", "Record added successfully");
        } else {
            model.addAttribute("errorMessage", "Record wasn't added; an error occurred");
        }
        return displayCreateRecordForm(table, model);
    }

    @GetMapping("/updateRecord")
    public String updateRecord(@RequestParam("table") String table,
                               @RequestParam("username") String username,
                               @RequestParam("id") String id,
                               Model model) {
        model.addAttribute("username",username);
        model.addAttribute("id",id);
        model.addAttribute("table", table);

        return "update_record";
    }

    @PostMapping("/updateRecord")
    public ModelAndView updateRecord(@RequestParam("table") String table,
                                     @RequestParam("username") String name,
                                     @RequestParam("id") String idToUpdate,
                                     @RequestParam("newId") String newId,
                                     @RequestParam("newUsername") String newUsername) {
        ModelAndView mav = new ModelAndView();
        List<String> columns = dao.getTableColumns(table);
        String primaryKeyColumn = columns.get(0);
        String secondaryKeyColumn = columns.get(1);

        if (!newId.matches("\\d{7}")) {
            return forwardWithError("Invalid ID format. ID must be a 7-digit numeric value.", table, name, idToUpdate);
        }

        if (newId.equals(idToUpdate)) {
            dao.updateRecord(table, secondaryKeyColumn,primaryKeyColumn, idToUpdate, newUsername);
            return new ModelAndView("redirect:/crud?role=ADMIN&table=" + table);
        }

        if (dao.isIdExists(newId, table)) {
            return forwardWithError("Error: " + newId + " ID already exists. Please enter a different ID.", table, name, idToUpdate);
        }
        dao.updateRecord(table,secondaryKeyColumn,primaryKeyColumn, idToUpdate, newUsername);
        boolean isUpdated = dao.updateRecord(table,primaryKeyColumn,primaryKeyColumn, idToUpdate, newId);

        if (isUpdated) {
            mav.setViewName("redirect:/crud?role=ADMIN&table=" + table);
        } else {
            return forwardWithError("No record was found with the specified ID.", table, name, idToUpdate);
        }

        return mav;
    }

    private ModelAndView forwardWithError(String errorMessage, String table, String name, String idToUpdate) {
        ModelAndView mav = new ModelAndView("update_record"); // Specify the view name for error message
        mav.addObject("errorMessage", errorMessage);
        mav.addObject("table", table);
        mav.addObject("username", name);
        mav.addObject("id", idToUpdate);
        return mav;
    }

    @GetMapping("/deleteRecord")
    public String deleteRecordPost(@RequestParam("table") String table,
                                   @RequestParam("id") String idToDelete,
                                   Model model) {
        System.out.println(table+" "+idToDelete);
        System.out.println(dao.deleteRecord(table, idToDelete));
        return crud(table,model);
    }
}