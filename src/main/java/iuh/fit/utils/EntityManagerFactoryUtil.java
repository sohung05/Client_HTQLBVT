package iuh.fit.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerFactoryUtil {
    private static EntityManagerFactory emf;
    private EntityManager em;

    public EntityManagerFactoryUtil() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("mssql-pu");
        }
        em = emf.createEntityManager();
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("mssql-pu");
        }
        return emf;
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
