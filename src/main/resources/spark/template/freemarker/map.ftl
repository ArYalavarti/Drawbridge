<#assign content>

<div class="loading-gif" id = "loading">
    <img src ="/images/loading.gif" style="width: 60px; height: auto;"/>
</div>

<div id="pre-load" style="visibility: visible;" class="search-menu-wrapper">
    <div class="search-menu">
        <h2 class="section-heading">Search for a Carpool</h2>
        <img src="/images/divider.png" style="height: 4px; width: auto;" />
        <form autocomplete="off" onsubmit="event.preventDefault(); handleSubmit()">
            <div class="search-inputs">
                <div><i class="fas fa-dot-circle icon-label"></i>
                    <input required class="address-input" id="start-input" onblur="handleInput('start-input', 0)" type="text" placeholder="Starting point..." />
                    <img id="loading-start-input" src ="/images/loading.gif" class="loading-address-gif"/>
                </div>
                <div><i class="fas fa-map-marker-alt icon-label"></i>
                    <input required class="address-input" id="end-input" onblur="handleInput('end-input', 1)" type="text" placeholder="Ending destination..." />
                    <img id="loading-end-input" src ="/images/loading.gif" class="loading-address-gif"/>
                </div>
                <div>
                    <i class="fas fa-calendar icon-label"></i>
                    <input required class="datetime-input flatpickr flatpickr-input" id="date" type="text" placeholder="Date..." />

                    <i class="fas fa-clock icon-label"></i>
                    <input required class="datetime-input flatpickr flatpickr-input" id="time" type="text" placeholder="Time..." />
                </div>
                <input name="submit" alt="Submit" type="image" src="/images/submit.png" class="submit-btn"/>
            </div>
        </form>
    </div>
</div>


<div id="pre-load" style="visibility: visible;" class="map-settings compass-setting" onclick="alignMap()">
    <img class="icon-img" src = "/images/compass-icon.png" />
</div>
<div id="pre-load" style="visibility: visible;" class="map-settings location-setting" onclick="centerMap()">
    <img class="icon-img" src = "/images/pin-icon.png" />
</div>
<div id="pre-load" style="visibility: hidden;" class="map-settings trip-setting" onclick="alignTrip()">
    <img id="car-icon" class="icon-img" src = "/images/car-disabled.png" />
</div>
<div id="pre-load" style="visibility: visible;" class="map-settings team-setting" onclick="toTeam()">
    <img class="icon-img" src = "/images/team-icon.png" />
</div>
<div id="pre-load" style="visibility: visible;" class="map-settings info-setting" onclick="toInfo()">
    <img class="icon-img" src = "/images/info-icon.png" />
</div>

<div id="route-info" style="visibility: hidden; flex-direction: column;" class="route-info">
    <div>Route Information</div>
    <img src="/images/divider.png" style="height: 2px; width: auto; margin-top: 2px; margin-bottom: 2px;" />
    <div>
        <div><i style="font-size: 10px;" class="fas fa-clock icon-label"></i><span style="font-size: 13px;" id = "duration"></span></div>
        <div><i style="font-size: 10px;" class="fas fa-road icon-label"></i><span style="font-size: 13px;" id = "distance"></span></div>
    </div>
</div>

<script src='/js/external/es6-promise.auto.min.js'></script>
<script src="/js/external/mapbox-sdk.min.js"></script>

<div id="map"></div>

<script src="/js/external/jquery-3.1.1.js"></script>
<script src="/js/map.js"></script>

</#assign>
<#include "main.ftl">
