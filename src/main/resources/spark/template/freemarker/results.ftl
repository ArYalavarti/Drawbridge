<#assign stylesheets>
    <link rel="stylesheet" href="/css/results.css"  type="text/css">
</#assign>

<#assign content>
    <div class="trip-info-wrapper">
        <div class="trip-info">
            <i class="fas fa-dot-circle icon-label-medium"></i>
                <span id="start" class="trip-info-text"></span>
            <img src="/images/divider.png" style="height: 2px; margin: 0px 10px; width: auto;" />
            <i class="fas fa-map-marker-alt icon-label-medium"></i>
                <span id="end" class="trip-info-text"></span>
        </div>
        <span class="sub-heading"><span id="date">Mar 14 2019</span> at <span id="time">1:59 PM</span></span>
    </div>

    <div class="results-container">
        <div class="results-content">
            <div class="thresholds">
                <div>
                    <span class="sub-heading">Walking Time</span>
                    <div><i class="fas fa-walking icon-label-large"></i>
                    <input class="threshold-input" id="walking-input" value="30" min="0" max="3600" type="number" placeholder="..." />min</div>
                </div>
                <div>
                    <span class="sub-heading">Departure Threshold</span>
                    <div><i class="far fa-circle icon-label-large"></i>
                    <input class="threshold-input" id="walking-input" value="15" min="0" max="3600" type="number" placeholder="..." />min</div>
                </div>
            </div>
            <span class="trip-info-text">Carpool Search Results</span>
        </div>
    </div>
    <script src="/js/results.js"></script>
</#assign>
<#include "main.ftl">