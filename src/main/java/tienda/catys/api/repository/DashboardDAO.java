package tienda.catys.api.repository;

import tienda.catys.api.dto.DashboardMetricsDTO;
import tienda.catys.api.repository.exception.DataAccessException;

public interface DashboardDAO {
    DashboardMetricsDTO getMetrics() throws DataAccessException;
}
