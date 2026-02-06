package co.tz.sheriaconnectapi;

import co.tz.sheriaconnectapi.Model.Entities.Authority;
import co.tz.sheriaconnectapi.Model.Entities.Role;
import co.tz.sheriaconnectapi.Repositories.AuthorityRepository;
import co.tz.sheriaconnectapi.Repositories.RoleRepository;
import jakarta.persistence.Entity;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class BootstrapDataLoader implements CommandLineRunner {

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;

    private static final List<String> ACTIONS = List.of(
            "CREATE",
            "READ",
            "UPDATE",
            "DELETE"
    );

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    @Override
    public void run(String... args) {
        System.out.println("🚀 Bootstrapping authorities and SUPER_ADMIN role...");

        // ==========================
        // 1️⃣ Generate Authorities
        // ==========================
        Reflections reflections =
                new Reflections("co.tz.sheriaconnectapi.Model.Entities");

        Set<Class<?>> entities =
                reflections.getTypesAnnotatedWith(Entity.class);

        Set<Authority> allAuthorities = new HashSet<>();

        for (Class<?> entityClass : entities) {

            String entityName = entityClass.getSimpleName().toUpperCase();

            for (String action : ACTIONS) {

                String authorityName = entityName + "_" + action;

                Authority authority = authorityRepository
                        .findByName(authorityName)
                        .orElseGet(() -> {
                            Authority a = new Authority();
                            a.setName(authorityName);
                            Authority saved = authorityRepository.save(a);
                            System.out.println("✅ Created authority: " + authorityName);
                            return saved;
                        });

                allAuthorities.add(authority);
            }
        }

        // ==========================
        // 2️⃣ Create SUPER_ADMIN role
        // ==========================
        Role superAdminRole = roleRepository
                .findByName(SUPER_ADMIN_ROLE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(SUPER_ADMIN_ROLE);
                    Role saved = roleRepository.save(role);
                    System.out.println("✅ Created role: SUPER_ADMIN");
                    return saved;
                });

        // ==========================
        // 3️⃣ Assign ALL authorities
        // ==========================
        if (superAdminRole.getAuthorities() == null) {
            superAdminRole.setAuthorities(new HashSet<>());
        }

        superAdminRole.getAuthorities().addAll(allAuthorities);
        roleRepository.save(superAdminRole);

        System.out.println("🎉 SUPER_ADMIN role now has all authorities");
    }
}
