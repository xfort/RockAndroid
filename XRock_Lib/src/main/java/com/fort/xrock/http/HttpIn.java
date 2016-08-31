package com.fort.xrock.http;

import java.io.InputStream;

/**
 * Created by Mac on 16/8/29.
 */
public interface HttpIn {

    public void doRequest(RockRequest request);

    public InputStream doRequestSync(RockRequest request);

}
