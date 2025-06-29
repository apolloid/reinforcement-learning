package adiputra.reinforcementlearning.chapter5.controllers;

import javax.swing.*;

import adiputra.reinforcementlearning.chapter5.models.Example5_1Model;
import adiputra.reinforcementlearning.chapter5.views.Example5_1View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * The Controller for Example 5.1, following the Model-View-Controller (MVC) pattern.
 * This class acts as the intermediary between the {@link Example5_1Model} and the {@link Example5_1View}.
 *
 * Its primary responsibilities are:
 * <ul>
 * <li>Listening for user actions from the view (e.g., clicking the simulate button).</li>
 * <li>Validating user input for the number of episodes.</li>
 * <li>Initiating the simulation process in the model on a background thread using a {@link SwingWorker}
 * to prevent the GUI from freezing.</li>
 * <li>Listening for updates from the model (via {@link PropertyChangeEvent}) such as progress,
 * status messages, and the final simulation result.</li>
 * <li>Updating the view with these changes to reflect the current state of the simulation.</li>
 * </ul>
 *
 * @see Example5_1Model
 * @see Example5_1View
 */
public class Example5_1Controller implements PropertyChangeListener {
    private final Example5_1Model model;
    private final Example5_1View view;
    private int episodes;

    /**
     * Constructs the controller and sets up the necessary listeners to connect the model and view.
     * It registers itself as a listener for property changes in the model and sets up a listener
     * for the simulation request action in the view.
     *
     * @param model The application's model, containing the simulation logic.
     * @param view  The application's view, responsible for displaying data and capturing user input.
     */
    public Example5_1Controller(Example5_1Model model, Example5_1View view) {
        this.model = model;
        this.view = view;

        this.model.addPropertyChangeListener(this);
        this.view.addSimulateListener(e -> handleSimulationRequest());
    }

    /**
     * Handles the user's request to start the simulation.
     * It parses the number of episodes from the view's input field, validates the input,
     * and initiates a background task using {@link SwingWorker} to run the simulation
     * defined in the model. It also manages the UI state (e.g., disabling the simulate button)
     * during the simulation.
     */
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

    /**
     * Responds to property changes from the model.
     * This method is called when the model updates its state (e.g., progress, status, or completion).
     * It ensures that all UI updates are performed safely on the Event Dispatch Thread (EDT)
     * by using {@link SwingUtilities#invokeLater}. Depending on the property that changed,
     * it updates the view's progress bar, status label, or displays the final result charts.
     *
     * @param evt A {@link PropertyChangeEvent} object describing the event source
     * and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        SwingUtilities.invokeLater(() -> {
            switch (propertyName) {
                case Example5_1Model.PROGRESS_PROPERTY:
                    view.setProgress((Integer) evt.getNewValue());
                    break;
                case Example5_1Model.STATUS_PROPERTY:
                    view.setStatus((String) evt.getNewValue());
                    break;
                case Example5_1Model.RESULT_PROPERTY:
                	Example5_1Model.SimulationResult result = (Example5_1Model.SimulationResult) evt.getNewValue();
                    view.showResultCharts(result.usableAceGrid(), result.nonUsableAceGrid(), episodes);
                    view.setStatus("Status: Completed");
                    break;
            }
        });
    }
}