package iuh.fit.gui.menu.swing.table;

import iuh.fit.gui.menu.model.ModelStudent;

public interface EventAction {

    public void delete(ModelStudent student);

    public void update(ModelStudent student);
}

