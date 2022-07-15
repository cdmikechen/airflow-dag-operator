package org.apache.airflow.database;

import io.agroal.api.AgroalDataSource;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import java.sql.*;
import java.util.Optional;

@ApplicationScoped
public class DatasourceService {

    private final AgroalDataSource defaultDataSource;

    public DatasourceService(Instance<AgroalDataSource> defaultDataSources) {
        this.defaultDataSource = defaultDataSources.stream().findFirst().orElse(null);
    }

    /**
     * Get airflow dag detail
     */
    public Optional<AirflowDag> getAirflowDag(String dagId) throws SQLException {
        try (Connection conn = defaultDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("select dag_id, is_paused from dag where dag_id=?")) {
            pstmt.setString(1, dagId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AirflowDag dag = new AirflowDag();
                    dag.setDagId(rs.getString(1));
                    dag.setPaused(rs.getBoolean(2));
                    return Optional.of(dag);
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    /**
     * Check if dag is imported error
     */
    public boolean importErrorDags(String filename) throws SQLException {
        try (Connection conn = defaultDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("select 1 from import_error where filename=?")) {
            pstmt.setString(1, filename);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
