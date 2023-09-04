package com.example.project_cs426_runningapp.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentEditProfileBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.project_cs426_runningapp.ViewModel.HomeViewModel


class EditProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var db: FirebaseFirestore
    private var email: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        database = Firebase.database.reference
        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        email = sharedPreferences.getString("email", "") ?: ""

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //fetch data
        CoroutineScope(Dispatchers.Main).launch {
                var users = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                for (document in users) {
                    var name = document.data?.get("fullname") as String
                    var address = document.data?.get("address") as String
                    var country = document.data?.get("country") as String
                    var email = document.data?.get("email") as String
                    var nickName = document.data?.get("password") as String
                    var phoneNumber = document.data?.get("phone") as String
                    var genre = document.data?.get("sex") as String
                    Log.d("name", document.data?.get("fullname").toString())
                    // Update the UI on the main thread
                    binding.FullNameInput.text = Editable.Factory.getInstance().newEditable(name ?: "")
                    binding.AddressInput.text = Editable.Factory.getInstance().newEditable(address ?: "")
                    binding.CountryInput.text = Editable.Factory.getInstance().newEditable(country ?: "")
                    binding.EmailInput.text = Editable.Factory.getInstance().newEditable(email ?: "")
                    binding.NickNameInput.text = Editable.Factory.getInstance().newEditable(nickName ?: "")
                    binding.PhoneInput.text = Editable.Factory.getInstance().newEditable(phoneNumber ?: "")
                    binding.GenreInput.text = Editable.Factory.getInstance().newEditable(genre ?: "")
                }
            }
        val items = listOf<String>("Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegowina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Congo, the Democratic Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia (Hrvatska)", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "France Metropolitan", "French Guiana", "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard and Mc Donald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao, People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia, The Former Yugoslav Republic of", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia, Federated States of", "Moldova, Republic of", "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Seychelles", "Sierra Leone", "Singapore", "Slovakia (Slovak Republic)", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "Spain", "Sri Lanka", "St. Helena", "St. Pierre and Miquelon", "Sudan", "Suriname", "Svalbard and Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan, Province of China", "Tajikistan", "Tanzania, United Republic of", "Thailand", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands (British)", "Virgin Islands (U.S.)", "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Yugoslavia", "Zambia", "Zimbabwe", "Palestine");

        val autoComplete = binding.CountryInput

        val adapter = ArrayAdapter(requireContext(), R.layout.country_list, items)
        autoComplete.setAdapter(adapter)

        autoComplete.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i)
            Toast.makeText(requireContext(), "Item: $itemSelected", Toast.LENGTH_SHORT).show()
        })
        binding.ProfileEditBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.SubmitBtn.setOnClickListener {
            val fullname = binding.FullNameInput.text.toString()
            val password = binding.NickNameInput.text.toString()
            val phone = binding.PhoneInput.text.toString()
            val country = binding.CountryInput.text.toString()
            val sex = binding.GenreInput.text.toString()
            val address = binding.AddressInput.text.toString()
            HomeViewModel.set(fullname)
            Log.d("EDIT MODEL", HomeViewModel.get())
            Log.d("NAME", "NAME IS : $fullname")
            val user = hashMapOf(
                "fullname" to fullname,
                "email" to email,
                "password" to password,
                "phone" to phone,
                "country" to country,
                "sex" to sex,
                "address" to address
            )

            // Commit Changes to FireBase
            db.collection("users")
                .document(email)
                .set(user)
                .addOnSuccessListener {
                    // The data was successfully written to Firestore
                }
                .addOnFailureListener { e ->
                    Log.e("TAG", "Error writing document: $e")
                }
            findNavController().popBackStack()
        }
    }
}