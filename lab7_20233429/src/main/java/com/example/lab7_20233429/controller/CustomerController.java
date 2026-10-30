package com.example.lab7_20233429.controller;


import com.example.lab7_20233429.entity.Customer;
import com.example.lab7_20233429.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/clientes")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping({"", "/"})
    public String listarClientes(Model model) {
        model.addAttribute("listaClientes", customerRepository.findAll());
        return "customer/list";
    }

    @GetMapping("/new")
    public String nuevoCliente(@ModelAttribute("customer") Customer customer) {
        return "customer/form";
    }

    @GetMapping("/edit")
    public String editarCliente(@ModelAttribute("customer") Customer customer,
                                @RequestParam("id") Integer id,
                                Model model) {
        Optional<Customer> opt = customerRepository.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("customer", opt.get());
            return "customer/form";
        }
        return "redirect:/clientes";
    }

    @PostMapping("/save")
    public String guardarCliente(@ModelAttribute("customer") @Valid Customer customer,
                                 BindingResult bindingResult,
                                 RedirectAttributes attr) {

        if (customer.getDocumentType() != null && !customer.getDocumentType().isBlank()) {
            String tipo = customer.getDocumentType();
            String numero = customer.getDocumentNumber();

            if (!tipo.equals("DNI") && !tipo.equals("RUC")) {
                bindingResult.rejectValue("documentType", "error.customer", "El tipo de documento solo puede ser DNI o RUC");
            } else if (numero != null && !numero.isBlank()) {
                if (tipo.equals("DNI") && !numero.matches("\\d{8}")) {
                    bindingResult.rejectValue("documentNumber", "error.customer", "El DNI debe contener exactamente 8 dígitos");
                }
                if (tipo.equals("RUC") && !numero.matches("\\d{11}")) {
                    bindingResult.rejectValue("documentNumber", "error.customer", "El RUC debe contener exactamente 11 dígitos");
                }
            }
        }

        // 2. Validar Unicidad del Número de Documento en la BD
        if (customer.getDocumentNumber() != null && !customer.getDocumentNumber().isBlank()) {
            Optional<Customer> existente = customerRepository.findByDocumentNumber(customer.getDocumentNumber());
            // Rechazar si existe Y (es registro nuevo O es una edición pero el ID no es el mismo)
            if (existente.isPresent() && (customer.getId() == null || !existente.get().getId().equals(customer.getId()))) {
                bindingResult.rejectValue("documentNumber", "error.customer", "Este número de documento ya se encuentra registrado en el sistema. Ingrese uno diferente.");
            }
        }

        // 3. Evaluar Errores y mantener estado
        if (bindingResult.hasErrors()) {
            // Retorna al form sin redirección para no perder los datos digitados ni los errores
            return "customer/form";
        }

        customerRepository.save(customer);
        attr.addFlashAttribute("msg", "Cliente guardado exitosamente");
        return "redirect:/clientes";
    }
}