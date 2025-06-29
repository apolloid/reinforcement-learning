package adiputra.reinforcementlearning.chapter5.controllers;

import javax.swing.*;

import adiputra.reinforcementlearning.chapter5.models.Example5_3Model;
import adiputra.reinforcementlearning.chapter5.views.Example5_3View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Example5_3Controller implements PropertyChangeListener {
    private final Example5_3Model model;
    private final Example5_3View view;
    private int episodes;

    public Example5_3Controller(Example5_3Model model, Example5_3View view) {
        this.model = model;
        this.view = view;

        this.model.addPropertyChangeListener(this);
        this.view.addSimulateListener(e -> handleSimulationRequest());
    }

    private void handleSimulationRequest() {
        try {
            episodes = Integer.parseInt(view.getEpisodes());
        } catch (NumberFormatException ex) {
            view.setStatus("Error: Please enter a valid number.");
            return;
        }

        view.setSimulateButtonEnabled(false);
        view.setProgress(0);
        view.setStatus("Status: Starting simulation...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                model.runSimulation(episodes);
                return null;
            }

            @Override
            protected void done() {
                view.setSimulateButtonEnabled(true);
            }
        };
        worker.execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        SwingUtilities.invokeLater(() -> {
            switch (propertyName) {
                case Example5_3Model.PROGRESS_PROPERTY:
                    view.setProgress((Integer) evt.getNewValue());
                    break;
                case Example5_3Model.STATUS_PROPERTY:
                    view.setStatus((String) evt.getNewValue());
                    break;
                case Example5_3Model.RESULT_PROPERTY:
                	Example5_3Model.SimulationResult result = (Example5_3Model.SimulationResult) evt.getNewValue();
                    view.showResultCharts(result.usableAceGrid(), result.nonUsableAceGrid(), episodes);
                    view.setStatus("Status: Completed");
                    break;
            }
        });
    }
}
