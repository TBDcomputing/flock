package com.tbdcomputing.network.leaderelection;

import com.tbdcomputing.network.leaderelection.state.ElectionFollower;
import com.tbdcomputing.network.leaderelection.state.ElectionState;
import com.tbdcomputing.network.leaderelection.state.ElectionStateContext;

/**
 * Created by dpho on 3/12/16.
 */
public class LeaderElection {
    private ElectionState state;

    public LeaderElection() {
        state = new ElectionFollower(new ElectionStateContext());
    }

}
