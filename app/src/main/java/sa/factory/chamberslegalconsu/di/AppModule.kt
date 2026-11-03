package sa.factory.chamberslegalconsu.di

import sa.factory.chamberslegalconsu.data.repository.ChambersLegalRepositoryImpl
import sa.factory.chamberslegalconsu.domain.repository.ChambersLegalRepository
import sa.factory.chamberslegalconsu.domain.usecase.*
import sa.factory.chamberslegalconsu.hardware.HardwareSensorDriver
import sa.factory.chamberslegalconsu.ui.viewmodel.ChambersLegalViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: ChambersLegalRepository = ChambersLegalRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): ChambersLegalViewModel = ChambersLegalViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
