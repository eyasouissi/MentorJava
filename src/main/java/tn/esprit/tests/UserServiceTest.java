package tn.esprit.tests;

import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import java.util.HashSet;
import java.util.Set;

public class UserServiceTest {
    public static void main(String[] args) {
        UserService userService = UserService.getInstance();

        // Test student user creation, modification and deletion
        User student = new User("modif@esprit.tn", "modif", "studentpass");
        Set<String> studentRoles = new HashSet<>();
        studentRoles.add("ROLE_STUDENT");
        student.setRoles(studentRoles);
        student.setAge(Integer.valueOf(20));
        student.setGender("Male");
        student.setCountry("Germany");
        student.setBio("Computer science student");

        // 1. Create the student
        userService.ajouter(student);
        System.out.println("\n[CREATE] Student created: " + student.getEmail() +
                " (ID: " + student.getId() + ")");

        // 2. Modify the student's name
        student.setName("pepemodif");
        userService.modifier(student);
        System.out.println("[UPDATE] Modified name to: " + student.getName());

        // 3. Now test deletion
        System.out.println("\n[DELETE] Attempting to delete user: pepemodif...");
        if(student.getId() != null) {
            try {
                userService.supprimer(student.getId().intValue());

                // Verify deletion
                User deletedUser = userService.getOne(student);
                if(deletedUser == null) {
                    System.out.println("✅ SUCCESS: User pepemodif was successfully deleted");
                } else {
                    System.out.println("❌ FAILURE: User still exists after deletion");
                }
            } catch (Exception e) {
                System.out.println("❌ ERROR during deletion: " + e.getMessage());
            }
        } else {
            System.out.println("❌ ERROR: User ID was null, cannot delete");
        }

        // Test getting all users to verify deletion
        System.out.println("\n=== Final User List ===");
        userService.getAll().forEach(user -> {
            System.out.println("\nUser: " + user.getName() + " (" + user.getEmail() + ")");
            System.out.println("Roles: " + user.getRoles());
        });
    }
}