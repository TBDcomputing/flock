package com.tbdcomputing.network.leaderelection;

import com.tbdcomputing.network.leaderelection.state.ElectionFollower;
import com.tbdcomputing.network.leaderelection.state.ElectionState;
import com.tbdcomputing.network.leaderelection.state.ElectionStateContext;
import com.tbdcomputing.network.leaderelection.state.ElectionStateType;

/**
 * Created by dpho on 3/12/16.
 */
public class ElectionManager {
    private ElectionState state = new ElectionFollower(new ElectionStateContext());
    private final ElectionListener listener = new ElectionListener() {
        @Override
        public void onTimeout() {
            state = state.transition(ElectionStateType.CANDIDATE);
        }
    };

    public ElectionManager() {
    }

}
