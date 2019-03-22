<#assign content>

<div style="width: 300px; text-align: center; position: absolute; left: 0; top: 40%; right: 0; margin-left: auto; margin-right: auto;" id = "loading">
    <img src ="/images/loading.gif" style="width: 60px; height: auto;"/>
</div>

<div id="pre-load" style="visibility: hidden;" class="search-menu-wrapper">
    <div class="search-menu">
        <h2 class="section-heading">Search for a Carpool</h2>
        <div class="search-inputs">
            <div><i class="fas fa-dot-circle icon-label"></i>
                <input class="address-input" id="start-input" onblur="handleInput('start-input', 0)" type="text" placeholder="Starting point...">
            </div>
            <div><i class="fas fa-map-marker-alt icon-label"></i>
                <input class="address-input" id="end-input" onblur="handleInput('end-input', 1)" type="text" placeholder="Ending destination...">
            </div>
        </div>
    </div>
</div>

<div id="pre-load" style="visibility: hidden;" class="map-settings compass-setting" onclick="alignMap()">
    <i class="fas fa-compass icon-map-settings"></i>
</div>

<div id="pre-load" style="visibility: hidden;" class="map-settings location-setting" onclick="centerMap()">
    <i class="fas fa-map-pin icon-map-settings"></i>
</div>

<script src='https://unpkg.com/es6-promise@4.2.4/dist/es6-promise.auto.min.js'></script>
<script src="https://unpkg.com/@mapbox/mapbox-sdk/umd/mapbox-sdk.min.js"></script>

<div id="map"></div>

<script src="/js/jquery-3.1.1.js"></script>
<script src="/js/map.js"></script>

</#assign>
<#include "main.ftl">
