package com.ykjm.todomap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.ykjm.todomap.databinding.FragmentMapBinding
import java.io.IOException

class MapFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 지도 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 검색 버튼 클릭 이벤트 설정
        val searchButton: Button = view.findViewById(R.id.Current_location)
        val mapSearch: EditText = view.findViewById(R.id.map_Search)

        searchButton.setOnClickListener {
            val location = mapSearch.text.toString()
            if (location.isNotEmpty()) {
                searchLocation(location)
            }
        }

        // 현재 위치 버튼 클릭 이벤트 설정
        val myLocationButton: FloatingActionButton = view.findViewById(R.id.btnMyLocation)
        myLocationButton.setOnClickListener {
            getDeviceLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 위치 권한을 요청
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        // 마커 클릭 리스너 설정
        mMap.setOnMarkerClickListener(this)
    }

    // 위치 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                    getDeviceLocation()
                }
            } else {
                // 권한이 거부된 경우 사용자에게 메시지를 표시
                Snackbar.make(binding.root, "위치 권한이 필요합니다.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // 현재 위치 가져오기
    private fun getDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 위치 검색 및 지도 업데이트
    private fun searchLocation(location: String) {
        val geocoder = Geocoder(requireContext())
        try {
            val addressList: List<Address>? = geocoder.getFromLocationName(location, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                val addressInfo = address.getAddressLine(0) ?: "No address available"
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(location)
                        .snippet(addressInfo)
                )
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                marker?.showInfoWindow() // 마커 생성 후 정보 창 표시
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Snackbar.make(binding.root, "위치를 찾을 수 없습니다.", Snackbar.LENGTH_LONG).show()
        }
    }

    // 마커 클릭 이벤트 처리
    override fun onMarkerClick(marker: Marker): Boolean {
        marker.showInfoWindow()
        AlertDialog.Builder(requireContext())
            .setTitle("위치 선택 확인")
            .setMessage("이 위치로 할 일을 설정하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                // 사용자가 확인을 선택한 경우, 선택한 위치 정보를 TodoDetailActivity로 전달합니다.
                val bundle = Bundle().apply {
                    putString("selectedLocationName", marker.title)
                }
                parentFragmentManager.setFragmentResult("requestKey", bundle)
                // 마커 선택 후 맵 프래그먼트를 닫습니다.
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Binding 객체 정리
        _binding = null
    }
}